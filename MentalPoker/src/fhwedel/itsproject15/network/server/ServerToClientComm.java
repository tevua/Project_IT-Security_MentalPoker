package fhwedel.itsproject15.network.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;

import fhwedel.itsproject15.network.server.ServerUsingTLS;

/**
 * Thread for the communication of the server to one client. 
 * @author tevua
 *
 */
public class ServerToClientComm implements Runnable {
	
	/** the server */
	ServerUsingTLS server;

	/** the name of the client */
	Integer client;

	/** the reader for the input stream */
	BufferedReader reader;

	/**
	 * Construktor.
	 * 
	 * @param aServer
	 *            the server
	 * @param s
	 *            the client socket
	 * @param aClient
	 *            name of the client
	 **/
	public ServerToClientComm(ServerUsingTLS aServer, SSLSocket s, int aClient) {
		this.server = aServer;
		this.client = aClient;
		try {
			final BufferedInputStream in = new BufferedInputStream(s.getInputStream());
			final InputStreamReader inputstreamreader = new InputStreamReader(
					in);
			this.reader = new BufferedReader(inputstreamreader);
		} catch (IOException e) {
			server.clientLeft(aClient);
		}
	}

	/**
	 * process the input stream
	 */
	protected void processInput() {
		boolean notEndOfStream = true;
		while (notEndOfStream) {
			try {
				final String line = reader.readLine();

				// Client is gone
				if (line == null) {
					this.server.clientLeft(this.client);
					notEndOfStream = false;

				} else {
					this.server.received(line);
				}
			} catch (IOException e) {
				this.server.clientLeft(this.client);
				notEndOfStream = false;
			}
		}
	}

	/**
	 * the run method
	 */
	public void run() {
		processInput();
	}
}
