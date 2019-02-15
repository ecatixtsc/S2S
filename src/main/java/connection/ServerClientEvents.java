package connection;

/**
 * Server-client events.
 * 
 * @author ecaterina.mccormick
 *
 */
public enum ServerClientEvents {

	/** The server-client socket was closed. */
	SOCKET_CLOSED,

	/** The server had received the client handshake packet. */
	RECEIVED_HANDSHAKE;
}
