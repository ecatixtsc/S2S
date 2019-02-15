package connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Timer;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.Coordinates3D;
import protocol.Coordinates3D.Coordinates3DMessage;
import protocol.ObjectModel;
import protocol.ObjectModel.ObjectModelPacket;
import protocol.Pedestrian;
import protocol.Pedestrian.PedestrianInformation;
import protocol.SimulatorHandshake.SimulatorHandshakeMessage;
import protocol.Vehicle;
import protocol.Vehicle.VehicleInformation;
import utilities.UtilityLogger;

public class LassieLiteClientEngine extends Observable {

	/** Logger for the class. */
	private static final Logger LOGGER = UtilityLogger.getLogger(LassieLiteClientEngine.class);

	/** Device publisher id as received in a handshake message. */
	private String receivedPublisherId = null;

	/** Server-client socket state. */
	private boolean isConnected = false;

	/** Remote client information. */
	private String remoteSocketInfo;

	/** Server-client socket. */
	private Socket serverClient = null;

	/** Raw output stream. */
	private OutputStream outputStream = null;

	/** Raw input stream. */
	private InputStream inputStream = null;

	/** Send thread. */
	private Runnable sendThread = null;

	/** Receive thread. */
	private Runnable receiveThread = null;

	/** List of objects received Eg. Vehicle, Pedestrian etc. */
	private LinkedList<Object> objectsToSend = new LinkedList<Object>();

	/** List of objects sent Eg. Vehicle, Pedestrian etc. */
	private LinkedList<Object> objectsReceived = new LinkedList<Object>();

	private ObjectModelPacket.Builder objectToSendBuilder = ObjectModel.ObjectModelPacket.newBuilder();

	/** Read/ Write time resolution [ms]. */
	private static int TIME_RESOLUTION = 100;

	/**
	 * Apoptosis is a process of programmed cell death. If the client socket doesn't
	 * receive a handshake packet within HANDSHAKE_TIMEOUT, the timer closes the
	 * socket.
	 */
	private Timer apoptosisTimer = new Timer();

	/** Time of the last ping. */
	private long timeOfLastPing = 0;

	/**
	 * Handshake timeout. If no identification message comes from the client, the
	 * server will disconnect it.
	 */
	public static final long HANDSHAKE_TIMEOUT = 600000;

	/** Ping interval in ms. */
	private static final int PING_INTERVAL_MS = 30000;

	public LassieLiteClientEngine(final Socket newServerClient) throws IOException {
		initializeStreams(newServerClient.getOutputStream(), newServerClient.getInputStream());

		this.serverClient = newServerClient;
		this.remoteSocketInfo = newServerClient.toString();
	}

	/**
	 * It configures the input streams and the JSON mapper.
	 * 
	 * @param newOutputStream socket output stream
	 * @param newInputStream  socket input stream
	 * @throws IOException on socket stream error
	 */
	private void initializeStreams(final OutputStream newOutputStream, final InputStream newInputStream)
			throws IOException {

		this.outputStream = newOutputStream;
		this.inputStream = newInputStream;

		outputStream.flush();

		isConnected = true;
		timeOfLastPing = System.currentTimeMillis();

		// if the handshake doesn't arrive in HANDSHAKE_TIMEOUT ms, the connection is
		// closed
		// apoptosisTimer.schedule(new TimerTask() {
		// @Override
		// public void run() {
		// if (receivedPublisherId == null || receivedPublisherId.isEmpty()) {
		// notifyClientSocketClosed("Timeout on handshake");
		// }
		// }
		// }, HANDSHAKE_TIMEOUT);
	}

	public final void startThreads() {

		// send data to the simulator
		sendThread = new Runnable() {
			private ObjectModelPacket.Builder packet = ObjectModel.ObjectModelPacket.newBuilder();
			private SimulatorHandshakeMessage.Builder handshakeMessage = SimulatorHandshakeMessage.newBuilder();
			private PedestrianInformation.Builder pedestrianMsg = Pedestrian.PedestrianInformation.newBuilder();
			private VehicleInformation.Builder vehicleMsg = Vehicle.VehicleInformation.newBuilder();
			private Coordinates3DMessage.Builder coordinatesMsg = Coordinates3D.Coordinates3DMessage.newBuilder();
			private Coordinates3DMessage.Builder headingMsg = Coordinates3D.Coordinates3DMessage.newBuilder();

			public void clearBuilders() {
				coordinatesMsg.clear();
				headingMsg.clear();
				handshakeMessage.clear();
				pedestrianMsg.clear();
				vehicleMsg.clear();
				packet.clear();
			}

			public void run() {
				Object objectToSend = null;

				while (isConnected) {
					try {
						if (serverClient.isConnected() && !serverClient.isClosed()) {
							objectToSend = null;

							// read from the list of objects to send
							synchronized (objectsToSend) {
								if (!objectsToSend.isEmpty()) {
									objectToSend = objectsToSend.removeFirst();
								}
							}

							// send object to the simulator
							if (objectToSend != null) {
								if (objectToSend instanceof ObjectModelPacket) {
									ObjectModelPacket objectModel = (ObjectModelPacket) objectToSend;

									clearBuilders();
									packet.mergeFrom(objectModel);
									packet.build().writeDelimitedTo(outputStream);
									outputStream.flush();
								} else {
									LOGGER.warn("Lassie Lite holds internally corrupted packets");
								}
							} else {
								if ((System.currentTimeMillis() - timeOfLastPing) > PING_INTERVAL_MS) {
									outputStream.flush();
								}
							}
						} else {
							isConnected = false;
						}
					} catch (Exception ex) {
						LOGGER.warn("Send: Client-server IO exception");
						isConnected = false;
					}
				}
			}
		};

		// receive data from the simulator
		receiveThread = new Runnable() {
			private ObjectModelPacket.Builder packet = ObjectModel.ObjectModelPacket.newBuilder();

			public void clearBuilders() {
				packet.clear();
			}

			public void run() {
				int nofProtocolBufferErrors = 0;
				ObjectModelPacket.Builder packet = ObjectModel.ObjectModelPacket.newBuilder();
				int nof = 0;

				while (isConnected) {
					try {
						if (serverClient.isConnected() && !serverClient.isClosed()) {

							if (inputStream.available() > 0) {
								clearBuilders();

								nof = inputStream.available();

								packet.clear();
								packet.mergeDelimitedFrom(inputStream);
								packet.build();
								nofProtocolBufferErrors = 0;

								if (packet.hasPedestrian()) {
									PedestrianInformation pedestrian = packet.getPedestrian();

									synchronized (objectsReceived) {
										objectsReceived.add(pedestrian);
									}

								} else if (packet.hasVehicle()) {
									VehicleInformation vehicle = packet.getVehicle();

									synchronized (objectsReceived) {
										objectsReceived.add(vehicle);
									}

								} else if (packet.hasHandshake()) {
									SimulatorHandshakeMessage handshake = packet.getHandshake();

									receivedPublisherId = handshake.getPublisherId();
									TIME_RESOLUTION = (int) handshake.getTickLength();
								}
							}
						} else {
							close("socket is not connected");
						}
					} catch (InvalidProtocolBufferException ex1) {
						nofProtocolBufferErrors++;

						System.err.println("err");

						if (nofProtocolBufferErrors > 10) {
							LOGGER.error("The client is sending too many incomprehesible packets ");
							close(ex1.getMessage());
						}
					} catch (Exception ex2) {
						close(ex2.getMessage());
					}
				}
			}
		};

		// new Thread(sendThread).start();
		new Thread(receiveThread).start();
	}

	/**
	 * Notify the server the socket connection with the client was closed.
	 * 
	 * @return
	 */
	private void notifyClientSocketClosed(String message) {
		LOGGER.info("Client closed, reason: " + message);
		setChanged();
		notifyObservers(ServerClientEvents.SOCKET_CLOSED);
	}

	/**
	 * Notify the server a handshake was received, the client sent its identity.
	 * 
	 */
	protected final void notifyReceivedPublisherId() {
		setChanged();
		notifyObservers(ServerClientEvents.RECEIVED_HANDSHAKE);
	}

	public void close(String reason) {
		isConnected = false;

		try {
			serverClient.close();
		} catch (Exception e) {
		}

		try {
			inputStream.close();
		} catch (Exception e) {
		}

		try {
			outputStream.close();
		} catch (Exception e) {
		}

		notifyClientSocketClosed(reason);
	}

	public Socket getServerClient() {
		return serverClient;
	}

	public Object getObjectFromInternalList() {
		Object internalObject = null;

		synchronized (objectsReceived) {
			if (!objectsReceived.isEmpty()) {
				internalObject = objectsReceived.removeFirst();
			}
		}

		if (internalObject != null) {
			return internalObject;
		}

		return null;
	}

	/**
	 * Wrap the object into a protobuf class and send it to the socket. Timestamp
	 * with socket time.
	 * 
	 * @param objectModel
	 */
	public void sendObjectToSim(Object objectModel) {
		if (serverClient.isConnected() && !serverClient.isClosed()) {
			objectToSendBuilder.clear();

			try {
				if (objectModel instanceof PedestrianInformation) {

					PedestrianInformation pedestrian = (PedestrianInformation) objectModel;
					objectToSendBuilder.setPedestrian(pedestrian);

				} else if (objectModel instanceof VehicleInformation) {

					VehicleInformation vehicle = (VehicleInformation) objectModel;
					objectToSendBuilder.setVehicle(vehicle);

				}

				// the difference, measured in milliseconds, between the current time and
				// midnight, January 1, 1970 UTC
				objectToSendBuilder.setSocketTimestamp(System.currentTimeMillis());
				objectToSendBuilder.build().writeDelimitedTo(outputStream);

				// for testing purposes
				// byte buffer[] = objectToSendBuilder.build().toByteArray();

				outputStream.flush();

			} catch (InvalidProtocolBufferException ex1) {
				LOGGER.warn("Wrong packet in the broadcast " + ex1.getMessage());
			} catch (IOException e) {
				close(e.getMessage());
			}
		}
	}
}
