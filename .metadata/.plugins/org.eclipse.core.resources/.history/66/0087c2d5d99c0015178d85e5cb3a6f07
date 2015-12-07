package de.fhwedel.itsproject1516.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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
	 */
	public void connect(String host, int port, String fnTrust, String pwTrust, String fnKey, String pwKey, int acceptSelfSigned, InputStream inputStream, boolean saveSelfSigned) {
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
			
			TrustManager[] byPassTrustManagers = new TrustManager[] { new OwnTrustManager(origManager, acceptSelfSigned, inputStream, saveSelfSigned, fnTrust, pwTrust) };
			
			/** load the own certificate */
			char[] passwordKey = pwKey.toCharArray();
			KeyManagerFactory kmf;

			File keyFile = new File(fnKey);
			inKeystore = new FileInputStream(keyFile);
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(inKeystore, passwordKey);

			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keystore, passwordKey);

			/** open the connection */
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextInt();

			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(kmf.getKeyManagers(), byPassTrustManagers, secureRandom);
			SSLSocketFactory sslsocketfactory = sslContext.getSocketFactory();
			this.tlsSocket = (SSLSocket) sslsocketfactory.createSocket(host, port);

			tlsSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
				public void handshakeCompleted(HandshakeCompletedEvent e) {
					// System.out.println("SSL handshake completed (client): ");
					// showHandshakeInfo(e);
				}
			});

			// established connection
			//this.log("Connected to " + host + "/" + port);

			// initialize the output stream
			this.out = new BufferedOutputStream(this.tlsSocket.getOutputStream());

			// waiting for communication with the server
			communication = new CommunicationClient(this, this.tlsSocket);
			final Thread waitingForCommunication = new Thread(communication, "Client ");
			waitingForCommunication.start();

		} catch (IOException e) {
			//this.log("Error: Unable to connect (IOException)");
		} catch (NoSuchAlgorithmException e) {
			//this.log("Error: Unable to connect (NoSuchAlgorithmException)");
		} catch (KeyManagementException e) {
			//this.log("Error: Unable to connect (KeyManagementException)");
		} catch (CertificateException e) {
			//this.log("Error: Unable to connect (CertificateException)");
		} catch (KeyStoreException e) {
			//this.log("Error: Unable to connect (KeyStoreException)");
		} catch (UnrecoverableKeyException e1) {
			//this.log("Error: Unable to connect (UnrecoverableKeyException)");
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
		//this.log("Sending: " + message);
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
}
