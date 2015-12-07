package de.fhwedel.itsproject1516.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

import org.bouncycastle.asn1.x500.X500Name;

public class NetworkGame {
	private static final String TRUST = "mentalpoker_chat";
	private static final String TRUSTFILE = TRUST + ".public";
	private static final String PASSWORD_TRUST = "secret";

	private static final String ANNA = "mentalpoker_anna_chat";
	private static final String BOB = "mentalpoker_bob_chat";
	private static final String KEY_ANNA = ANNA + ".private";
	private static final String KEY_BOB = BOB + ".private";
	private static final String PASSWORD_KEY = "password";

	private static void createCerts(){
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
		} catch (Exception e) {
			
		}
	}
	
	public static void main(String[] args) {
		
		//createCerts(); 
		TLSNetwork network = null; 
		if (args.length == 1) {
			int port = Integer.parseInt(args[0]);
			network = new TLSNetwork(TLSNetwork.SERVER);
			network.start(port, TRUSTFILE, PASSWORD_TRUST, KEY_ANNA, PASSWORD_KEY, OwnTrustManager.NEVER, null, false);
		} else
		if (args.length == 2) {
			String host = args[0];
			int port = Integer.parseInt(args[1]);
			 network = new TLSNetwork(TLSNetwork.CLIENT);
			 network.connect(host, port, TRUSTFILE, PASSWORD_TRUST, KEY_BOB, PASSWORD_KEY, OwnTrustManager.NEVER, null,
					false);
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    String s;
	    try {
			while ((s = in.readLine()) != null && s.length() != 0) {
				network.send(s);
			}  
		} catch (IOException e) {
			e.printStackTrace();
			network.stop();
		}
	    network.stop();
	}
}
