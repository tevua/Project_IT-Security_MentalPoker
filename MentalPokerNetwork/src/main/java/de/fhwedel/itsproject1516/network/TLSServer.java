package de.fhwedel.itsproject1516.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class TLSServer {
	/** the server socket */
	private SSLServerSocket tlsServerSocket;

	/** the sockets of the clients */
	private HashMap<Integer, Socket> clients;

	/** the clients the server communicates with */
	private HashMap<Integer, CommunicationServer> comWithClients;

	/** the player **/
	private TLSNetwork networkComm;

	/**
	 * Constructor.
	 */
	public TLSServer(TLSNetwork nc) {
		super();
		clients = new HashMap<Integer, Socket>();
		comWithClients = new HashMap<Integer, CommunicationServer>();
		this.networkComm = nc;
	}

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
	public void start(int port, String fnTrust, String pwTrust, String fnKey, String pwKey, int acceptSelfSigned, InputStream inputStream) {
		try {
			KeyStore keystore;

			/** load the trusted certificates */
			char[] passwordTrust = pwTrust.toCharArray();
			TrustManagerFactory tmf;

			File trustFile = new File(fnTrust);
			FileInputStream inKeystore = new FileInputStream(trustFile);
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(inKeystore, passwordTrust);

			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keystore);
			
			X509TrustManager origManager = null;
			TrustManager[] tms = tmf.getTrustManagers();
			int i = 0;
			while (i < tms.length && origManager == null) {
				if (tms[i] instanceof X509TrustManager) {
					origManager = (X509TrustManager) tms[i];
				}
				++i;
			}
			
			TrustManager[] byPassTrustManagers = new TrustManager[] { new OwnTrustManager(origManager, acceptSelfSigned, inputStream, false) };
			
			/** load the own certificate */
			char[] passwordKey = pwKey.toCharArray();
			KeyManagerFactory kmf;

			File keyFile = new File(fnKey);
			inKeystore = new FileInputStream(keyFile);
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(inKeystore, passwordKey);

			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, passwordKey);
			
			/** open the server */
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextInt();

			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(kmf.getKeyManagers(), byPassTrustManagers, secureRandom);
			SSLServerSocketFactory sslserversocketfactory = sslContext.getServerSocketFactory();
			this.tlsServerSocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);
			
			this.tlsServerSocket.setNeedClientAuth(true);
			//this.log("Server listening on port " + port);

			final ServerThread runnable = new ServerThread(this);
			final Thread waitingForClient = new Thread(runnable, "waitingForClientsToConnect");
			waitingForClient.start();
		} catch (IOException e) {
			//this.log("Error: Unable to start server");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Stops the server and all open connections.
	 */
	public void stop() {
		try {
			final Collection<Socket> allSockets = this.clients.values();
			final Iterator<Socket> i = allSockets.iterator();
			while (i.hasNext()) {
				final Socket c = i.next();
				c.close();
			}
			this.tlsServerSocket.close();
			//this.log("Server stopped");
		} catch (IOException e) {
			System.out.println("Error closing the server.");
		}
	}

	/**
	 * Returns the server socket.
	 * 
	 * @return the server socket
	 */
	public SSLServerSocket getServerSocket() {
		return this.tlsServerSocket;
	}

	/**
	 * A new client has connected .
	 * 
	 * @param s
	 *            socket of the client
	 * @param input
	 *            Anmeldenachricht des Clients.
	 */
	public synchronized void newClient(SSLSocket s) {
		// future implementation: waiting for more clients:
		/*
		 * final ServerThread runnable = new ServerThread(this); final Thread
		 * waitingForClient = new Thread(runnable,
		 * "waitingForClientsToConnect"); waitingForClient.start();
		 */

		// identifier of the client
		Integer clientId = 1;
		while (clients.get(clientId) != null) {
			clientId++;
		}
		clients.put(clientId, s);

		//this.log("Client registered with id: " + clientId);

		this.networkComm.newPlayerConnected(clientId);

		// waiting for communication with the client
		final CommunicationServer communication = new CommunicationServer(this, s, clientId);
		this.comWithClients.put(clientId, communication);
		final Thread waitingForCommunication = new Thread(communication, "communicationWithClient" + clientId);
		waitingForCommunication.start();
	}

	/**
	 * A client left.
	 * 
	 * @param clientId
	 *            the id of the client
	 */
	public void clientLeft(Integer clientID) {
		try {
			clients.get(clientID).close();
		} catch (IOException e) {
			System.out.println("IOException trying to close socket of client " + clientID);
		}
		clients.remove(clientID);
		//this.log("Client " + clientID + " left");
	}

	/**
	 * Sends a single message to a client.
	 * 
	 * @param c
	 *            Socket of the client
	 * @param message
	 *            the message
	 * @throws IOException
	 *             if something goes wrong.
	 */
	private synchronized void sendMessageToClient(Socket s, String message) throws IOException {
		final BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream());
		final PrintWriter printer = new PrintWriter(out);
		printer.println(message);
		printer.flush();
	}

	/**
	 * A message is received.
	 * 
	 * @param message
	 *            the message
	 */
	public synchronized void received(String message) {
		//this.log("Received: " + message);
		this.networkComm.receivedMessage(message);

	}

	/**
	 * Sending a message to all registered clients.
	 * 
	 * @param the
	 *            message
	 */
	public synchronized void send(String message) {
		//this.log("Sending: " + message);
		final Collection<Socket> allSockets = this.clients.values();
		final Iterator<Socket> i = allSockets.iterator();
		while (i.hasNext()) {
			final Socket c = i.next();
			try {
				sendMessageToClient(c, message);
			} catch (IOException e) {
				//this.log("Error sending message");
			}
		}
	}
}
