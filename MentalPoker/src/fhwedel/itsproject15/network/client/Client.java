package fhwedel.itsproject15.network.client;

import java.io.IOException;

import fhwedel.itsproject15.network.Loggable;

/**
 * Interface for a client.
 * 
 * @author tevua
 */
public interface Client extends Loggable {

	/**
	 * Establishes a connection to a given host on a given port.
	 * 
	 * @param host
	 *            the host address
	 * @param port
	 *            port where the connection is supposed to be established
	 */
	void connect(String host, int port);

	/**
	 * Abandons the connection to the server.
	 */
	void disconnect();

	/**
	 * Sends a message to the server
	 * 
	 * @param message
	 *            the message to be sent
	 * @throws IOException
	 *             thrown when it is not possible to write in the output stream
	 */
	void send(String message);
}
