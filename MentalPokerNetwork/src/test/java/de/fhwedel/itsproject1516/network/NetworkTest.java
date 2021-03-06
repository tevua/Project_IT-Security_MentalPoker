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

	private static final String TRUST = "mentalpoker2";
	private static final String TRUSTFILE = TRUST + ".public";
	private static final String TRUST_MOD = "test_root_modify";
	private static final String TRUSTFILE_MOD = TRUST_MOD + ".public";
	private static final String PASSWORD_TRUST = "secret";

	private static final String ANNA = "mentalpoker_anna";
	private static final String BOB = "mentalpoker_bob";
	private static final String KEY_ANNA = ANNA + ".private";

	private static final String KEY_BOB = BOB + ".private";

	private static final String CHARLES = "mentalpoker_charles";
	private static final String KEY_SELF_SIGNED = CHARLES + ".private";

	private static final String DAISY = "mentalpoker_daisy";
	private static final String KEY_SELF_SIGNED2 = DAISY + ".private";

	private static final String ED = "signed_by_daisy";
	private static final String KEY_NOT_TRUSTED = ED + ".private";

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
					TRUST, PASSWORD_TRUST, "testroot", true);

			X509CertGenerator genKeys = new X509CertGenerator(new BigInteger("6"));
			genKeys.loadRoot(TRUST + ".private", PASSWORD_TRUST, "testroot");

			genKeys.createCert(2048, ANNA, PASSWORD_KEY, "test42clientanna",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516,CN=Test Anna"), false);

			genKeys.createCert(2048, BOB, PASSWORD_KEY, "test42clientbob",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516,CN=Test Bob"), false);

			X509CertGenerator genSelfSigned1 = new X509CertGenerator(new BigInteger("8"));
			genSelfSigned1.createRoot(new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITSProject WS1516, CN=Charles"),
					2048, CHARLES, PASSWORD_KEY, "charles", false);

			X509CertGenerator genSelfSigned2 = new X509CertGenerator(new BigInteger("9"));
			genSelfSigned2.createRoot(new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITSProject WS1516, CN=Daisy"),
					2048, DAISY, PASSWORD_KEY, "daisy", false);

			genSelfSigned2.createCert(2048, ED, PASSWORD_KEY, "test42cliented",
					new X500Name("C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516,CN=Ed"), false);

			X509CertGenerator genMod = new X509CertGenerator(new BigInteger("11"));
			genMod.createRoot(new X500Name("C=GERMANY,L=Wedel,O=FH Wedel, OU=ITSProject WS1516, CN=Charles"), 2048,
					TRUST_MOD, PASSWORD_TRUST, "testrootmod", false);

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
		deleteFile(TRUST_MOD + ".private");
		deleteFile(TRUSTFILE_MOD);
	}

	@Test
	public void simpleNetworkTest() {
		// System.out.println("simpleNetworkTest");
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
		// System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));
	}

	@Test
	public void acceptSelfSignedTestAlways() {
		// System.out.println("acceptSelfSignedTestAlways");
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
		// System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));

	}

	@Test
	public void notAcceptSelfSigned() {
		// System.out.println("notAcceptSelfSigned");
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
		// System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 0, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 0, clientMessages.size());

	}

	@Test
	public void permitSelfSigned() {
		// System.out.println("permitSelfSigned");
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
		// System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));
	}

	@Test
	public void testNotTrustedCert() {
		// System.out.println("testNotTrustedCert");
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
		// System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 0, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 0, clientMessages.size());
	}

	@Test
	public void testSaveSelfSignedCert() {
		// start server (or client) with a self signed cert, tell them to save
		// it
		// System.out.println("testSaveSelfSignedCert");
		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, TRUSTFILE_MOD, PASSWORD_TRUST, KEY_ANNA, PASSWORD_KEY, OwnTrustManager.ALWAYS, null, true);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED, PASSWORD_KEY, OwnTrustManager.NEVER,
				null, false);
		shortDelay();
		client.stop();
		shortDelay();
		server.stop();
		shortDelay();

		// start server again but this time without accepting self signed certs,
		// should still send messages

		TLSNetwork server2 = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client2 = new TLSNetwork(TLSNetwork.CLIENT);

		server2.start(PORT, TRUSTFILE_MOD, PASSWORD_TRUST, KEY_ANNA, PASSWORD_KEY, OwnTrustManager.NEVER, null, false);
		shortDelay();

		client2.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_SELF_SIGNED, PASSWORD_KEY, OwnTrustManager.NEVER,
				null, false);
		shortDelay();

		client2.send(MESSAGE_TO_SERVER);
		server2.send(MESSAGE_TO_CLIENT);

		shortDelay();

		client2.stop();

		shortDelay();
		server2.stop();

		List<String> serverMessages = server2.getAllMessages();
		List<String> clientMessages = client2.getAllMessages();
		// System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 1, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 1, clientMessages.size());
		Assert.assertEquals("server received a wrong message", MESSAGE_TO_SERVER, serverMessages.get(0));
		Assert.assertEquals("client received a wrong message", MESSAGE_TO_CLIENT, clientMessages.get(0));
	}

	@Test
	public void testSignedByDaisy() {
		// System.out.println("testSignedByDaisy");
		TLSNetwork server = new TLSNetwork(TLSNetwork.SERVER);
		TLSNetwork client = new TLSNetwork(TLSNetwork.CLIENT);

		server.start(PORT, DAISY + ".public", PASSWORD_KEY, KEY_BOB, PASSWORD_KEY, OwnTrustManager.ALWAYS, null, false);
		shortDelay();

		client.connect(HOST, PORT, TRUSTFILE, PASSWORD_TRUST, KEY_NOT_TRUSTED, PASSWORD_KEY, OwnTrustManager.ALWAYS,
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
		// System.out.println();
		Assert.assertEquals("wrong number of messages received by the server.", 0, serverMessages.size());
		Assert.assertEquals("wrong number of messages received by the client.", 0, clientMessages.size());
	}
}