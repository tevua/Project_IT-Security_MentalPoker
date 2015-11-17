package fhwedel.itsproject15.network.server;

import java.io.IOException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

import javax.net.ssl.SSLSocket;

import fhwedel.itsproject15.network.server.ServerUsingTLS;

/**
 * The server thread waiting for clients to connect.
 */

public class ServerThread implements Runnable {
	/**
	 * the server
	 */
	ServerUsingTLS server;

	/**
	 * Constructor.
	 * 
	 * @param aServer
	 *            the server
	 **/
	public ServerThread(ServerUsingTLS aServer) {
		this.server = aServer;

	}

	/**
	 * the run method
	 */
	public void run() {
		try {
			final SSLSocket s = (SSLSocket) this.server.getServerSocket().accept();
			((SSLSocket) s).addHandshakeCompletedListener(new HandshakeCompletedListener() {
				public void handshakeCompleted(HandshakeCompletedEvent e) {
					// System.out.println("SSL handshake completed server");
				}
			});
			server.newClient(s);
		} catch (IOException e) {
			this.server.stop();
		}
	}
}