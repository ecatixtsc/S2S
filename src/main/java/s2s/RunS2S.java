package s2s;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import connection.LassieLiteClientEngine;
import connection.ServerClientEvents;
import utilities.UtilityLogger;

public class RunS2S {

	/** Logger for the class. */
	private static final Logger LOGGER = UtilityLogger.getLogger(RunS2S.class);

	/** Send thread. */
	private Runnable sendThread = null;

	/** Receive thread. */
	private Runnable receiveThread = null;

	/** Server-client socket state. */
	private boolean isConnected = false;

	/** Server socket. */
	private static ServerSocket serverSocket = null;

	/** Server close state. */
	private static boolean serverClosed = false;

	/** Socket server-client connection map indexed by SocketAddress. */
	private final static Map<SocketAddress, LassieLiteClientEngine> workerConnectionMap = new HashMap<SocketAddress, LassieLiteClientEngine>();

	/** Listen to client requests thread. */
	private static Runnable listenToConnectionsThread = null;

	/** Broadcasting thread. */
	private static Runnable broadcastToClients = null;

	private static int portToListen = 5050;

	public static void main(String[] args) {

		// configure the logger
		BasicConfigurator.configure();

		/*
		 * try { CoordinatesTransformation.transform1("", ""); } catch
		 * (MismatchedDimensionException e1) { e1.printStackTrace(); } catch
		 * (NoSuchAuthorityCodeException e1) { e1.printStackTrace(); } catch
		 * (FactoryException e1) { e1.printStackTrace(); } catch (TransformException e1)
		 * { e1.printStackTrace(); }
		 */

		// start a server socket
		try {
			serverSocket = new ServerSocket(portToListen);
			LOGGER.info("The server is alive...");
		} catch (IOException e) {
			LOGGER.error("SERVER could not be created on the port " + portToListen, e);
		}

		Observer clientsObserver = new Observer() {

			@Override
			public void update(Observable observable, Object notification) {
				if (notification instanceof ServerClientEvents) {
					ServerClientEvents event = (ServerClientEvents) notification;

					switch (event) {
					case RECEIVED_HANDSHAKE:
						LOGGER.info("Handshake received");
						break;
					case SOCKET_CLOSED:
						LOGGER.info("Client closed");

						if (observable instanceof LassieLiteClientEngine) {
							LassieLiteClientEngine client = (LassieLiteClientEngine) observable;

							synchronized (workerConnectionMap) {
								workerConnectionMap.remove(client.getServerClient().getRemoteSocketAddress());
								client.deleteObserver(this);
							}
						}
						break;
					}
				}

			}
		};

		// listen to client sockets in a separate thread
		listenToConnectionsThread = new Runnable() {
			@Override
			public void run() {
				while (!serverClosed) {
					try {
						LOGGER.info("Listening to clients...");
						listenToConnectionBlockingAndStartThread(clientsObserver);
					} catch (Exception e) {
						LOGGER.error("Devices Server error ", e);
					}
				}
			}
		};
		new Thread(listenToConnectionsThread).start();

		// broadcast messages to clients
		broadcastToClients = new Runnable() {
			@Override
			public void run() {

				while (!serverClosed) {
					List<LassieLiteClientEngine> listOfClients = null;

					synchronized (workerConnectionMap) {
						listOfClients = new ArrayList<LassieLiteClientEngine>(workerConnectionMap.values());
					}

					int nofClients = listOfClients.size();

					// if (listOfClients == null || nofClients <= 1) {
					if (listOfClients == null) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					}

					for (int i = 0; i < nofClients; i++) {
						LassieLiteClientEngine clientToSend = listOfClients.get(i);

						for (int j = 0; j < nofClients; j++) {
							if (i != j) {
								LassieLiteClientEngine clientBroadcasting = listOfClients.get(j);
								Object objectModel = null;

								do {
									objectModel = clientBroadcasting.getObjectFromInternalList();

									if (objectModel != null) {
										clientToSend.sendObjectToSim(objectModel);
									}
								} while (objectModel != null);								
							}
						}
						
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		new Thread(broadcastToClients).start();

		// listen to console and close when a key is pressed
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			String line = in.readLine();
			in.close();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Listen to a client connection, blocking operation.
	 * 
	 * @param clientsObserver
	 */
	private static void listenToConnectionBlockingAndStartThread(Observer clientsObserver) {
		try {
			Socket newClientSocket = serverSocket.accept();
			SocketAddress newSocketAddress = newClientSocket.getRemoteSocketAddress();
			LassieLiteClientEngine newWorkerConnection = null;

			LOGGER.info("New client accepted " + newClientSocket.toString());

			// lock the map of connections to update
			synchronized (workerConnectionMap) {

				// the connection already existed in the map, remove the old one
				if (workerConnectionMap.containsKey(newSocketAddress)) {
					newWorkerConnection = workerConnectionMap.get(newSocketAddress);
					newWorkerConnection.close("connection already exited in the cache");
					workerConnectionMap.remove(newSocketAddress);
				}

				// add new connection
				newWorkerConnection = new LassieLiteClientEngine(newClientSocket);
				newWorkerConnection.addObserver(clientsObserver);
				workerConnectionMap.put(newSocketAddress, newWorkerConnection);

				LOGGER.info("The server holds " + workerConnectionMap.size() + " connections");
			}

			// start the reading/writing threads for the new connections
			newWorkerConnection.startThreads();

			LOGGER.info("SERVER received the connection from " + newClientSocket.getRemoteSocketAddress());

		} catch (Exception e) {
			LOGGER.error("Server accept connection error!", e);
		}
	}
}
