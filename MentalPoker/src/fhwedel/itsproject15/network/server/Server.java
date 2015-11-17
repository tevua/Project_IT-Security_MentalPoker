package fhwedel.itsproject15.network.server;

//import java.io.IOException;
//import java.util.List;

import fhwedel.itsproject15.network.Loggable;
//import fhwedel.itsproject15.network.Message;

/**
 * Interface for a server
 * 
 * @author tevua
 */
public interface Server extends Loggable {

	/**
	 * Starting the server on a given port. Trusted certificates are used to
	 * verify clients.
	 * 
	 * This method returns right away. Waiting for clients happens in another
	 * thread.
	 * 
	 * @param port
	 *            the port the server is started on.
	 */
	void start(int port);

	/**
	 * Stops the server and all open connections.
	 */
	void stop();

	/**
	 * Sending a message to all registered clients.
	 * 
	 * @param the
	 *            message
	 */
	void send(String message);

}
