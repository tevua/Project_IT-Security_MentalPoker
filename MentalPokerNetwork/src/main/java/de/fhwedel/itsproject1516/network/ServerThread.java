package de.fhwedel.itsproject1516.network;

import java.io.IOException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;

/**
 * The server thread waiting for clients to connect.
 */

public class ServerThread implements Runnable {
	/**
	 * the server
	 */
	private TLSServer server;

	/**
	 * the thread
	 */
	private volatile Thread serverThread;

	/**
	 * Constructor.
	 * 
	 * @param aServer
	 *            the server
	 **/
	public ServerThread(TLSServer aServer) {
		this.server = aServer;

	}

	public void start() {
		serverThread = new Thread(this);
		serverThread.start();
	}

	public void stop() {
		serverThread = null;
		System.out.println("stoppedserver");
	}

	/**
	 * the run method
	 */
	@Override
	public void run() {
		//Thread thisThread = Thread.currentThread();
		//while (serverThread == thisThread) {
			try {
				final SSLSocket s = (SSLSocket) this.server.getServerSocket().accept();
				((SSLSocket) s).addHandshakeCompletedListener(new HandshakeCompletedListener() {
					public void handshakeCompleted(HandshakeCompletedEvent e) {
						// System.out.println("SSL handshake completed
						// server");
					}
				});
				server.newClient(s);
			} catch (IOException e) {
				this.server.stop();
			}

		//}
		//System.out.println("end of run");
	}
}
