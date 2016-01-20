package de.fhwedel.itsproject1516.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class TLSNetwork {

	public static final boolean SERVER = true;
	public static final boolean CLIENT = false;

	/** if the network instance is a server this is not null */
	private TLSServer server;
	/** if the network instance is a client this is not null */
	private TLSClient client;

	/** List of messages */
	private List<String> messages;

	protected TLSNetworkGame game; 
	
	/**
	 * Constructor.
	 * 
	 * @param server
	 *            true if the network instance is a server
	 */
	public TLSNetwork(boolean server, TLSNetworkGame aGame) {
		messages = new LinkedList<String>();
		this.game = aGame; 
		if (server) {
			this.server = new TLSServer(this);
			this.client = null;
		} else {
			this.server = null;
			this.client = new TLSClient(this);
		}
	}

	/**
	 * Start the server (if the network instance is a client, nothing happens)
	 * 
	 * @param port
	 *            the port the server is supposed to start at
	 * @param trustFilename
	 *            the file where the trusted certificates are saved
	 * @param trustPassword
	 *            the password for the keystore of trustFilename
	 * @param keyFilename
	 *            the file where the key/certificate is saved
	 * @param keyPassword
	 *            the password for the keystore of the keyFilename
	 * @param acceptSelfSigned
	 *            is self signed certificates are supposed to be accepted
	 * @param inputStream
	 *            input stream (only needed if acceptSelfSigned == WITH_PERMIT
	 * @param saveSelfSigned
	 *            if self signed are supposed to be saved
	 */
	public void start(int port, String trustFilename, String trustPassword, String keyFilename, String keyPassword,
			int acceptSelfSigned, InputStream inputStream, boolean saveSelfSigned) {
		if (this.server != null) {
			try {
				this.server.start(port, getSSLContext(trustFilename, trustPassword, keyFilename, keyPassword,
						acceptSelfSigned, inputStream, saveSelfSigned));
			} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException
					| CertificateException | KeyStoreException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Connect a client to a server. If the network instance is a server nothing
	 * happens.
	 * 
	 * @param host
	 *            the host the client wants to connect to
	 * @param port
	 *            the port
	 * @param trustFilename
	 *            the file where the trusted certificates are saved
	 * @param trustPassword
	 *            the password for the keystore of trustFilename
	 * @param keyFilename
	 *            the file where the key/certificate is saved
	 * @param keyPassword
	 *            the password for the keystore of the keyFilename
	 * @param acceptSelfSigned
	 *            is self signed certificates are supposed to be accepted
	 * @param inputStream
	 *            input stream (only needed if acceptSelfSigned == WITH_PERMIT
	 * @param saveSelfSigned
	 *            if self signed are supposed to be saved
	 */
	public void connect(String host, int port, String trustFilename, String trustPassword, String keyFilename,
			String keyPassword, int acceptSelfSigned, InputStream inputStream, boolean saveSelfSigned) {
		if (this.client != null) {
			try {
				this.client.connect(host, port, getSSLContext(trustFilename, trustPassword, keyFilename, keyPassword,
						acceptSelfSigned, inputStream, saveSelfSigned));
			} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException
					| CertificateException | KeyStoreException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets called if a new player connected
	 * 
	 * @param clientId
	 *            the identifier of the client
	 */
	public void newPlayerConnected(int clientId) {
		// TODO
	}

	/**
	 * A message was received
	 * 
	 * @param message
	 *            the message
	 */
	public void receivedMessage(String message) {
		this.game.receivedMessage(message);
		messages.add(message);
	}

	/**
	 * The list of received messages
	 * 
	 * @return list of received messages
	 */
	public List<String> getAllMessages() {
		return this.messages;
	}

	/**
	 * Send a message.
	 * 
	 * @param message
	 *            the message
	 */
	public void send(String message) {
		if (this.client != null) {
			this.client.send(message);
		} else if (this.server != null) {
			this.server.send(message);
		}
	}

	/**
	 * Stops the network instance.
	 */
	public void stop() {
		if (this.client != null) {
			this.client.disconnect();
		} else if (this.server != null) {
			this.server.stop();
		}
		this.game.gameHasStopped(); 
	}

	/**
	 * Gets the public key of the peer we connected to.
	 * 
	 * @return the public key of the peer we connected to.
	 * @throws SSLPeerUnverifiedException
	 *             if the peer is unverified
	 */
	public PublicKey getOtherPubKey() throws SSLPeerUnverifiedException {
		if (this.client != null) {
			return this.client.getOtherPubKey();
		} else if (this.server != null) {
			return this.server.getOtherPubKey();
		} else {
			return null;
		}
	}

	/**
	 * Creates the ssl context.
	 * 
	 * @param trustFilename
	 *            the file where the trusted certificates are saved
	 * @param trustPassword
	 *            the password for the keystore of trustFilename
	 * @param keyFilename
	 *            the file where the key/certificate is saved
	 * @param keyPassword
	 *            the password for the keystore of the keyFilename
	 * @param acceptSelfSigned
	 *            is self signed certificates are supposed to be accepted
	 * @param inputStream
	 *            input stream (only needed if acceptSelfSigned == WITH_PERMIT
	 * @param saveSelfSigned
	 *            if self signed are supposed to be saved
	 * @return context for the secure connection
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws IOException
	 */
	private SSLContext getSSLContext(String trustFilename, String trustPassword, String keyFilename, String keyPassword,
			int acceptSelfSigned, InputStream inputStream, boolean saveSelfSigned)
					throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
					CertificateException, KeyStoreException, IOException {
		/** open the connection */
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextInt();

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(getKeyManagers(keyFilename, keyPassword),
				getTrustManagers(trustFilename, trustPassword, acceptSelfSigned, inputStream, saveSelfSigned),
				secureRandom);
		return sslContext;
	}

	/**
	 * Creates key managers
	 * 
	 * @param fnKey
	 *            the file where the key/certificate is saved
	 * @param pwKey
	 *            the password
	 * @return array of key managers
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 * @throws UnrecoverableKeyException
	 */
	private KeyManager[] getKeyManagers(String fnKey, String pwKey) throws NoSuchAlgorithmException,
			CertificateException, IOException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore;

		/** load the own certificate */
		char[] passwordKey = pwKey.toCharArray();
		KeyManagerFactory kmf;

		File keyFile = new File(fnKey);
		FileInputStream inKeystore = new FileInputStream(keyFile);
		keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(inKeystore, passwordKey);

		kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keystore, passwordKey);

		return kmf.getKeyManagers();
	}

	/**
	 * Creates the trust managers.
	 * 
	 * @param fnTrust
	 *            the file where the trusted certificates are saved
	 * @param pwTrust
	 *            the password
	 * @param acceptSelfSigned
	 *            value between 0 and 2
	 * @param inputStream
	 *            input stream of acceptSelfSigned ==
	 *            OwnTrustManager.WITH_PERMIT
	 * @param saveSelfSigned
	 *            true if the accepted self signed are supposed to be saved
	 * @return array of trust managers
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyStoreException
	 */
	private TrustManager[] getTrustManagers(String fnTrust, String pwTrust, int acceptSelfSigned,
			InputStream inputStream, boolean saveSelfSigned)
					throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
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

		return new TrustManager[] {
				new OwnTrustManager(origManager, acceptSelfSigned, inputStream, saveSelfSigned, fnTrust, pwTrust) };
	}
}
