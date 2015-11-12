import java.math.BigInteger;

import org.bouncycastle.asn1.x500.X500Name;

import fhwedel.itsproject15.network.cert.X509CertGen;

public class CertGenTest {

	public static void main(String[] args) throws Exception {

		X509CertGen gen = new X509CertGen(
				new X500Name(
						"C=GERMANY,L=Wedel,O=FH Wedel, OU=ITS Project WS1516, CN=Mental Poker"),
				new BigInteger("5"));

		/** Creating a root certificate */
		// the file "project_root.public" should be given to everyone who wants
		// to play and be used with the TrustManagerFactory
		gen.createRoot(2048, "project_root", "password", "test42root");
		// gen.loadRoot("project_root.private", "password", "test42root");

		/** Signing a certificate with the root certificate */
		// the file "project_edeltraud.private" should only be given to Edeltraud and be
		// used with the KeyManagerFactory - the
		// file "project_edeltraud.public" is not needed and thus not created
		gen.createCert(
				2048,
				"project_edeltraud",
				"password",
				"test42client",
				new X500Name(
						"C=Germany, L=Wedel, O=FH Wedel, OU=ITS Project WS1516, CN=Edeltraud"));
	}

}
