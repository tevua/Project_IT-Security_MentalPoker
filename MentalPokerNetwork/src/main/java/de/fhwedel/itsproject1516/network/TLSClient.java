package de.fhwedel.itsproject1516.network;

import java.io.BufferedOutputStream;

import java.io.IOException;

import java.io.PrintWriter;


import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSClient {
	/** the socket of the client */
	SSLSocket tlsSocket;

	/** the output stream */
	BufferedOutputStream out;

	/** the runnable where the communication is happening */
	private CommunicationClient communication;

	/** the player **/
	private TLSNetwork networkComm;

	/**
	 * The constructor.
	 * 
	 * @param player
	 *            the player/the game that is happening
	 */
	public TLSClient(TLSNetwork nc) {
		this.networkComm = nc;
	}

	/**
	 * Establishes a connection to a given host on a given port.
	 * 
	 * @param host
	 *            the host address
	 * @param port
	 *            port where the connection is supposed to be established
	 * @param sslContext
	 *            the Context for the SSL connection
	 */
	public void connect(String host, int port, SSLContext sslContext) {
		try {

			SSLSocketFactory sslsocketfactory = sslContext.getSocketFactory();
			this.tlsSocket = (SSLSocket) sslsocketfactory.createSocket(host, port);

			tlsSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
				public void handshakeCompleted(HandshakeCompletedEvent e) {
					// System.out.println("SSL handshake completed (client): ");
					// showHandshakeInfo(e);
				}
			});

			// established connection
			// this.log("Connected to " + host + "/" + port);

			// initialize the output stream
			this.out = new BufferedOutputStream(this.tlsSocket.getOutputStream());

			// waiting for communication with the server
			communication = new CommunicationClient(this, this.tlsSocket);
			final Thread waitingForCommunication = new Thread(communication, "Client ");
			waitingForCommunication.start();

		} catch (IOException e) {
			// this.log("Error: Unable to connect (IOException)");
		}
	}

	/**
	 * Abandons the connection to the server.
	 */
	public void disconnect() {
		try {
			tlsSocket.close();
		} catch (IOException e) {
			System.out.println("Error closing the connection.");
		}
	}

	/**
	 * Returns true if the client is connected to the server.
	 * 
	 * @return true, if connected
	 */
	public boolean isConnected() {
		return tlsSocket.isConnected();
	}

	/**
	 * Sends a message to the server
	 * 
	 * @param message
	 *            the message to be sent
	 * @throws IOException
	 *             thrown when it is not possible to write in the output stream
	 */
	public synchronized void send(String message) {
		final PrintWriter printer = new PrintWriter(this.out);
		printer.println(message);
		printer.flush();
		// this.log("Sending: " + message);
	}

	/**
	 * A message is received.
	 * 
	 * @param message
	 *            the message
	 */
	public synchronized void received(String message) {
		// this.log("Received: " + message);
		this.networkComm.receivedMessage(message);
	}
}
