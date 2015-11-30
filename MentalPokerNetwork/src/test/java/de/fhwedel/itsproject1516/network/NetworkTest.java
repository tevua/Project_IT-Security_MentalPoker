package de.fhwedel.itsproject1516.network;

import org.junit.Test;

import org.bouncycastle.asn1.x500.X500Name;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

/*
 * This Java source file was auto generated by running 'gradle init --type java-library'
 * by 'gypsy' at '29.11.15 15:23' with Gradle 2.6
 *
 * @author tevua, @date 29.11.15 15:23
 */
public class NetworkTest {

	private final String HOST = "localhost";
	private final int PORT = 9905;

	private final String TRUSTFILE = "root.public";

	private final String KEY_ANNA = "anna.private";

	private final String KEY_BOB = "bob.private";

	private final String KEYFILE_SELF_SIGNED = "charles.private";
	private final String KEYFILE_SELF_SIGNED2 = "charles2.private";
	
	private final String KEY_NOT_TRUSTED = "project_anna.private"; 

	private final String PASSWORD = "password";

	private final String MESSAGE_TO_SERVER = "a simple message";
	private final String MESSAGE_TO_CLIENT = "another simple message";

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			System.out.println("sry");
		}
	}

	private void shortDelay() {
		sleep(1000);
	}

	private void createCerts() {
	/*	X509CertGenerator rootGen = new X509CertGenerator(
				new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITS Project WS1516, CN=Mental Poker Root"),
				new BigInteger("5"));

		// Creating a root certificate
		try {
			rootGen.createRoot(2048, "root", PASSWORD, "test42root");

			// Signing a certificate with the root certificate
			rootGen.createCert(2048, "anna", PASSWORD, "test42client",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516, CN=Anna"));

			rootGen.createCert(2048, "bob", PASSWORD, "test42client",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516, CN=Bob"));

			X509CertGenerator selfSignGen = new X509CertGenerator(
					new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITS Project WS1516, CN=Charles"),
					new BigInteger("102"));

			// Creating a self-signed root certificate
			selfSignGen.createRoot(2048, "charles_self_signed", PASSWORD, "test42selfsigned");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	@Test
	public void simpleNetworkTest() {
		System.out.println("-- simpleNetworkTest --");
		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE, PASSWORD, KEY_ANNA, PASSWORD, OwnTrustManager.ALWAYS, null);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD, KEY_BOB, PASSWORD, OwnTrustManager.ALWAYS, null);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);

		shortDelay();

		client.stop();

		shortDelay();
		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();
		System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));
	}

	@Test
	public void acceptSelfSignedTestAlways() {
		System.out.println("-- acceptSelfSignedTestAlways --");
		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE, PASSWORD, KEYFILE_SELF_SIGNED, PASSWORD, OwnTrustManager.ALWAYS, null);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD, KEYFILE_SELF_SIGNED2, PASSWORD, OwnTrustManager.ALWAYS, null);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();

		System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));

	}

	@Test
	public void notAcceptSelfSigned() {
		System.out.println("-- notAcceptSelfSigned --");
		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE, PASSWORD, KEYFILE_SELF_SIGNED, PASSWORD, OwnTrustManager.NEVER, null);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD, KEYFILE_SELF_SIGNED2, PASSWORD, OwnTrustManager.NEVER, null);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();

		System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 0, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 0, clientMessages.size());

	}

	@Test
	public void permitSelfSigned() {
		System.out.println("-- permitSelfSigned --");
		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		String string = "j\n";
		InputStream stringStream = new java.io.ByteArrayInputStream(string.getBytes());

		server.start(PORT, TRUSTFILE, PASSWORD, KEYFILE_SELF_SIGNED2, PASSWORD, OwnTrustManager.ALWAYS,
				null);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD, KEYFILE_SELF_SIGNED, PASSWORD, OwnTrustManager.WITH_PERMIT,
				stringStream);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();
		System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));
	}
	
	@Test
	public void testNotTrustedCert() {
		System.out.println("-- testNotSElfSignedNotTrustedCert --");
		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		String string = "j\n";
		InputStream stringStream = new java.io.ByteArrayInputStream(string.getBytes());

		server.start(PORT, TRUSTFILE, PASSWORD, KEY_NOT_TRUSTED, PASSWORD, OwnTrustManager.ALWAYS,
				stringStream);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD, KEY_BOB, PASSWORD, OwnTrustManager.ALWAYS,
				stringStream);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();
		System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 0, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 0, clientMessages.size());
	}
}
