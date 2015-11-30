package de.fhwedel.itsproject1516.network;

import org.junit.Test;

import org.bouncycastle.asn1.x500.X500Name;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

/**
 * Test for the network implementation
 * 
 * @author tevua
 * @version 1.0
 */
public class NetworkTest {

	private final String HOST = "localhost";
	private final int PORT = 9905;

	private static final String TRUST = "test_root";
	private static final String TRUSTFILE = TRUST + ".public";
	private static final String PASSWORD_TRUST = "secret";

	private static final String ANNA = "mentalpoker_anna";
	private static final String BOB = "mentalpoker_bob";
	private static final String KEY_ANNA = ANNA + ".private";

	private static final String KEY_BOB = BOB + ".private";

	private static final String CHARLES = "mentalpoker_charles";
	private static final String KEY_SELF_SIGNED = CHARLES + ".private";

	private static final String DAISY = "mentalpoker_daisy";
	private static final String KEY_SELF_SIGNED2 = DAISY + ".private";

	private static final String ED = "ed";
	private static final String KEY_NOT_TRUSTED = "ed.private";

	private static final String PASSWORD_KEY = "password";

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

	@BeforeClass
	public static void createCerts() {
		try {

			X509CertGenerator genTrust = new X509CertGenerator(new BigInteger("5"));

			genTrust.createRoot(new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITSProject WS1516, CN=Test Root"), 2048,
					TRUST, PASSWORD_TRUST, "testroot");

			X509CertGenerator genKeys = new X509CertGenerator(new BigInteger("6"));
			genKeys.loadRoot(TRUST + ".private", PASSWORD_TRUST, "testroot");

			genKeys.createCert(2048, ANNA, PASSWORD_KEY, "test42clientanna",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516,CN=Test Anna"));

			genKeys.createCert(2048, BOB, PASSWORD_KEY, "test42clientbob",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516,CN=Test Bob"));

			X509CertGenerator genSelfSigned1 = new X509CertGenerator(new BigInteger("8"));
			genSelfSigned1.createRoot(new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITSProject WS1516, CN=Charles"),
					2048, CHARLES, PASSWORD_KEY, "charles");

			X509CertGenerator genSelfSigned2 = new X509CertGenerator(new BigInteger("9"));
			genSelfSigned2.createRoot(new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITSProject WS1516, CN=Daisy"),
					2048, DAISY, PASSWORD_KEY, "daisy");

			genSelfSigned2.createCert(2048, ED, PASSWORD_KEY, "test42cliented",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516,CN=Ed"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void deleteFile(String filename) {
		File file = new File(filename);

		if (file.delete()) {
			// System.out.println(file.getName() + " is deleted!");
		} else {
			// System.out.println("Delete operation is failed.");
		}
	}

	@AfterClass
	public static void deleteCerts() {
		deleteFile(TRUST + ".private");
		deleteFile(TRUSTFILE);
		deleteFile(KEY_ANNA);
		deleteFile(KEY_BOB);
		deleteFile(KEY_SELF_SIGNED);
		deleteFile(CHARLES + ".public");
		deleteFile(KEY_SELF_SIGNED2);
		deleteFile(DAISY + ".public");
		deleteFile(KEY_NOT_TRUSTED);
	}

	@Test
	public void simpleNetworkTest() {

		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE, PASSWORD_TRUST, KEY_ANNA, PASSWORD_KEY, OwnTrustManager.ALWAYS, null, false);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_BOB, PASSWORD_KEY, OwnTrustManager.ALWAYS, null,
				false);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);

		shortDelay();

		client.stop();

		shortDelay();
		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();

		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));
	}

	@Test
	public void acceptSelfSignedTestAlways() {

		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED, PASSWORD_KEY, OwnTrustManager.ALWAYS, null,
				false);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED2, PASSWORD_KEY, OwnTrustManager.ALWAYS,
				null, false);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();

		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));

	}

	@Test
	public void notAcceptSelfSigned() {

		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED, PASSWORD_KEY, OwnTrustManager.NEVER, null,
				false);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED2, PASSWORD_KEY, OwnTrustManager.NEVER,
				null, false);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();

		Assert.assertEquals("wrong number of messages received by the server.", 0, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 0, clientMessages.size());

	}

	@Test
	public void permitSelfSigned() {

		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		String string = "j\n";
		InputStream stringStream = new java.io.ByteArrayInputStream(string.getBytes());

		server.start(PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED2, PASSWORD_KEY, OwnTrustManager.ALWAYS, null,
				false);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED, PASSWORD_KEY,
				OwnTrustManager.WITH_PERMIT, stringStream, false);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();

		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));
	}

	@Test
	public void testNotTrustedCert() {

		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE, PASSWORD_TRUST, KEY_NOT_TRUSTED, PASSWORD_KEY, OwnTrustManager.ALWAYS, null,
				false);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_BOB, PASSWORD_KEY, OwnTrustManager.ALWAYS, null,
				false);
		shortDelay();

		client.send(MESSAGE_TO_SERVER);
		server.send(MESSAGE_TO_CLIENT);
		shortDelay();

		client.stop();
		shortDelay();

		server.stop();

		List<String> serverMessages = server.getAllMessages();
		List<String> clientMessages = client.getAllMessages();

		Assert.assertEquals("wrong number of messages received by the server.", 0, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 0, clientMessages.size());
	}

	@Test
	public void testSaveSelfSignedCert() {
		// TODO
		// start server (or client) with a self signed cert, tell them to save
		// it
		// start server again but this time without accepting self signed certs,
		// should still send messages
	}
}