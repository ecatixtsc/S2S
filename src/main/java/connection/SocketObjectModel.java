package connection;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Socket Object Model is used to wrap an HLA object over JSON.
 * 
 * @author ecaterina.mccormick
 *
 * @param <ObjectType>
 */
@JsonPropertyOrder({ "packettype", "packetdata" })
public class SocketObjectModel<ObjectType> {

	/** Packet type identifies the type of data contained. */
	private String packetType = null;

	/** The object HLA model wrapped in JSON stream. */
	private ObjectType packetData = null;

	/**
	 * Constructor with class type.
	 * 
	 * @param classType object model class type
	 */
	public SocketObjectModel(final Class<ObjectType> classType) {
		packetType = classType.getSimpleName();
	}

	/**
	 * The constructor in serialisation.
	 */
	public SocketObjectModel() {

	}

	/**
	 * Getter for the packet type.
	 * 
	 * @return packet type as text
	 */
	@JsonGetter("packettype")
	public final String getPacketType() {
		return packetType;
	}

	/**
	 * Setter for packet type.
	 * 
	 * @param newPacketType text input describing the packet type
	 */
	@JsonSetter("packettype")
	public final void setPacketType(final String newPacketType) {
		this.packetType = newPacketType;
	}

	/**
	 * Getter for the packet data. The HLA object is serialised and send over the
	 * socket.
	 * 
	 * @return packet type as object
	 */
	@JsonGetter("packetdata")
	public final ObjectType getPacketData() {
		return packetData;
	}

	/**
	 * Setter for packet type.
	 * 
	 * @param newPacket packet serialised into an json stream
	 */
	@JsonSetter("packetdata")
	public final void setPacketData(final ObjectType newPacket) {
		this.packetData = newPacket;
	}
}
