package fhwedel.itsproject15.network.cert;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Class used to generate a certificate using BC and java.security. Not
 * thoroughly tested yet, but seems to work. Not very pretty. Would be great if
 * in a future implementation it would be able to use BC's key generation.
 * 
 * The generated certificate is saved in two KeyStore files, to be used with the
 * Java Secure Socket Extension (JSSE).
 * 
 * Sources: http://www.bouncycastle.org/wiki/display/JA1/BC+Version+2+APIs
 * http:/
 * /stackoverflow.com/questions/29852290/self-signed-x509-certificate-with-
 * bouncy-castle-in-java
 * 
 * @author tevua
 * @version 1.0
 */
public class X509CertGen {

	/**
	 * Generate Key Pair for RSA Cipher Currently not used/needed as I haven't
	 * figured out how to use BCs AsymmetricCipherKeyPair instead of Java's
	 * KeyPair.
	 * 
	 * @return key pair (public and private)
	 */
	private AsymmetricCipherKeyPair generatRSAKeyPairBC(BigInteger pubExp,
			int strength, int certainty) {
		SecureRandom random = new SecureRandom();

		KeyGenerationParameters keyGenParam = new RSAKeyGenerationParameters(
				pubExp, random, strength, certainty);

		AsymmetricCipherKeyPairGenerator keyPairGen = new RSAKeyPairGenerator();
		keyPairGen.init(keyGenParam);

		return keyPairGen.generateKeyPair();
	}

	/**
	 * Generates an RSA Key Pair.
	 * 
	 * @param strength
	 *            the strength of the newly created key pair
	 * @return the key pair
	 * @throws NoSuchAlgorithmException
	 *             if the key pair generator thinks there is no algorithm for
	 *             generating RSA stuff...
	 */
	private KeyPair generateRSAKeyPair(int strength)
			throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(strength);
		return keyPairGenerator.genKeyPair();
	}

	private SubjectPublicKeyInfo getPublicKeyInfo(BigInteger modulus,
			BigInteger pubExponent) {
		try {
			return SubjectPublicKeyInfoFactory
					.createSubjectPublicKeyInfo(new RSAKeyParameters(false,
							modulus, pubExponent));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generate a self-signed X509 certificate using BC.
	 * 
	 * @param pair
	 *            RSA key pair
	 * @return the self signed certificate
	 * @throws Exception
	 *             if something goes wrong.. obviously
	 */
	private X509Certificate generateSelfSignedX509Certificate(KeyPair pair)
			throws Exception {

		ContentSigner sigGen;
		/**
		 * This should be used if BC is used to create the RSA key. Currently
		 * the key is generated using the Java Security class, as I haven't
		 * found out to make certicate generation work with BCs
		 * AsymmetricCipherKeyPair.
		 * 
		 * AlgorithmIdentifier sigAlgId = new
		 * DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
		 * AlgorithmIdentifier digAlgId = new
		 * DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
		 * 
		 * sigGen = new BcRSAContentSignerBuilder(sigAlgId,
		 * digAlgId).build(pair.getPrivate());
		 */

		PrivateKey privKey = pair.getPrivate();

		sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC")
				.build(privKey);

		// RSAKeyParameters pub = (RSAKeyParameters) pair.getPublic();
		RSAPublicKey pubKey = (RSAPublicKey) pair.getPublic();

		SubjectPublicKeyInfo subPubKeyInfo = getPublicKeyInfo(
				pubKey.getModulus(), pubKey.getPublicExponent());

		Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60
				* 1000); // yesterday
		Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60
				* 1000); // in a year

		// TODO: figure out what the certificate issuer and the certificate subject
		// are supposed to be/what better values than would be for it
		// TODO: implement a proper serial number instead of using BigInteger.ONE
		X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
				new X500Name("CN=Test"), BigInteger.ONE, startDate, endDate,
				new X500Name("CN=Test"), subPubKeyInfo);

		X509CertificateHolder certHolder = v1CertGen.build(sigGen);
		return new JcaX509CertificateConverter().setProvider("BC")
				.getCertificate(certHolder);
	}

	/**
	 * Stores the public certificate in a newly created file.
	 * 
	 * @param cert
	 *            The certificiate
	 * @param alias
	 *            alias for saving the certificate in the KeyStore
	 * @param pw
	 *            password for the KeyStore
	 * @param filename
	 *            name of the file (the file will eventually be called
	 *            <filename>.public
	 * @throws KeyStoreException
	 *             if something goes wrong with the KeyStore
	 * @throws IOException
	 *             most likely something goes wrong with the file
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	private void storePublic(X509Certificate cert, String alias, String pw,
			String filename) throws KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {
		char[] password = pw.toCharArray();

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);

		File keystoreFile = new File(filename + ".public");

		if (!keystoreFile.exists()) {
			keystoreFile.createNewFile();
		} else {
			// Load the keystore contents
			// TODO load existing file...
			// FileInputStream in = new FileInputStream(keystoreFile);
			// ks.load(in, password);
			// in.close();
		}

		// Add the certificate
		ks.setCertificateEntry(alias, cert);

		// Save the new keystore contents
		FileOutputStream out = new FileOutputStream(keystoreFile);
		ks.store(out, password);
		out.close();
	}

	/**
	 * Stores the "private" certificate (which means the certificate itself and
	 * the private key) in a newly created file.
	 * 
	 * @param cert
	 *            the certificate
	 * @param pair
	 *            they RSA key pair used to create the certificate
	 * @param alias
	 *            the alias which should be used to store the certificate in the
	 *            KeyStore
	 * @param pw
	 *            the password for the KeyStore
	 * @param filename
	 *            name of the file (the file will eventually be called
	 *            <filename>.private
	 * @throws KeyStoreException
	 *             if something goes wrong with the KeyStore
	 * @throws IOException
	 *             most likely something goes wrong with the file
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	private void storePrivate(X509Certificate cert, KeyPair pair, String alias,
			String pw, String filename) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {

		char[] password = pw.toCharArray();

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);

		File keystoreFile = new File(filename + ".private");

		keystoreFile.createNewFile();

		// Add the certificate
		ks.setCertificateEntry(alias, cert);

		// Save the new keystore contents
		FileOutputStream out = new FileOutputStream(keystoreFile);
		ks.store(out, password);
		out.close();

		// now add the private key to the new keystore
		ks.load(null, password);

		KeyFactory gen = KeyFactory.getInstance("RSA");
		PrivateKey privKey = pair.getPrivate();

		PrivateKeyEntry entry = new PrivateKeyEntry(privKey,
				new java.security.cert.Certificate[] { cert });
		ks.setEntry(alias, entry, new KeyStore.PasswordProtection(password));

		FileOutputStream fos = new FileOutputStream(keystoreFile);
		ks.store(fos, password);
		fos.close();
	}

	/**
	 * Creates a self signed certificate and saves it in two files (one is the
	 * public one to share and on is the private to not share).
	 * 
	 * @param strength
	 *            the strength of the RSA key used to generate the certificate
	 * @param filename
	 *            the filenames of the certificate (it will create two files:
	 *            <filename>.public and <filename>.private
	 * @param pwPrivate
	 *            the password for the private file
	 * @param pwPublic
	 *            the password for the public file
	 * @param alias
	 *            the alias for the staff that is supposed to be saved
	 * @throws Exception
	 *             if something goes wrong...
	 */
	public void createSelfSignedCertificateAndPrivateKey(int strength,
			String filename, String pwPrivate, String pwPublic, String alias)
			throws Exception {
		// adds the Bouncy castle provider to java security
		Security.addProvider(new BouncyCastleProvider());

		// not used anymore.. couldn't make it work using the bc generated rsa
		// key pair
		// AsymmetricCipherKeyPair keyPair = generatRSAKeyPair();
		KeyPair pair = generateRSAKeyPair(strength);
		X509Certificate cert = generateSelfSignedX509Certificate(pair);

		// cert.checkValidity(new Date());
		// cert.verify(cert.getPublicKey());

		storePrivate(cert, pair, alias, pwPrivate, filename);
		storePublic(cert, alias, pwPublic, filename);

		// System.out.println(cert);
	}
}