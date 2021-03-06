package de.fhwedel.itsproject1516.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class TLSNetwork {

	public static final boolean SERVER = true;
	public static final boolean CLIENT = false;

	private TLSServer server;
	private TLSClient client;

	private List<String> messages;

	public TLSNetwork(boolean server) {
		messages = new LinkedList<String>();
		if (server) {
			this.server = new TLSServer(this);
			this.client = null;
		} else {
			this.server = null;
			this.client = new TLSClient(this);
		}
	}

	public void start(int port, String trustFilename, String trustPassword, String keyFilename, String keyPassword,
			int acceptSelfSigned, InputStream inputStream, boolean saveSelfSigned) {
		if (this.server != null) {
			try {
				this.server.start(port,
						getSSLContext(trustFilename, trustPassword, keyFilename, keyPassword, acceptSelfSigned, inputStream, saveSelfSigned));
			} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException
					| CertificateException | KeyStoreException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void connect(String host, int port, String trustFilename, String trustPassword, String keyFilename,
			String keyPassword, int acceptSelfSigned, InputStream inputStream, boolean saveSelfSigned) {
		if (this.client != null) {
			try {
				this.client.connect(host,port, getSSLContext(trustFilename, trustPassword, keyFilename, keyPassword, acceptSelfSigned, inputStream, saveSelfSigned));
			} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException
					| CertificateException | KeyStoreException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void newPlayerConnected(int clientId) {
		// TODO
		System.out.println("new player with id: " + clientId);
	}

	public void receivedMessage(String message) {
		//TODO
		System.out.println(message);
		messages.add(message);
	}

	public List<String> getAllMessages() {
		return this.messages;
	}

	public void playerLeft(int clientId) {
		// TODO
		if (clientId == 0 && this.client != null) {
			// the server was apparently closed
			this.client.disconnect();
		} else {
			this.server.clientLeft(clientId);
		}
	}

	public void send(String message) {
		if (this.client != null) {
			this.client.send(message);
		} else if (this.server != null) {
			this.server.send(message);
		}
	}

	public void stop() {
		if (this.client != null) {
			this.client.disconnect();
		} else if (this.server != null) {
			this.server.stop();
		}
	}

	private SSLContext getSSLContext(String trustFilename, String trustPassword, String keyFilename, String keyPassword,
			int acceptSelfSigned, InputStream inputStream, boolean saveSelfSigned)
					throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
					CertificateException, KeyStoreException, IOException {
		/** open the connection */
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextInt();

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(getKeyManagers(keyFilename, keyPassword),
				getTrustManagers(trustFilename, trustPassword, acceptSelfSigned, inputStream, saveSelfSigned), secureRandom);
		return sslContext;
	}

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
