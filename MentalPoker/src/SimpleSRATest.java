
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.SRAPrivateKeyGenerator;
import org.bouncycastle.crypto.generators.SRAPublicKeyGenerator;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.params.SRAPrivKeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPubKeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPublicParameters;

/**
 * Just a simple test of my SRA implementation. 
 * @author tevua
 *
 */
public class SimpleSRATest {
	/**
	 * Generate Key Pair for SRA Cipher
	 * 
	 * @return key pair (public and private)
	 */
	private AsymmetricCipherKeyPair generateSRAKeyPair() {
		System.out.println("Generating the keys for SRA\n");

		SecureRandom random = new SecureRandom();
		int strength = 768;
		int certainty = 25;

		// Erstellen des öffentlichen Teil des Schlüssel
		System.out.println("starting the generation of public p, q and n....");

		KeyGenerationParameters keyGenParm = new SRAPubKeyGenerationParameters(
				random, strength, certainty);

		SRAPublicKeyGenerator genPub = new SRAPublicKeyGenerator();
		genPub.init(keyGenParm);

		CipherParameters ciph = genPub.generateKeyPair();
		System.out.println(".... done");

		SRAPublicParameters p = (SRAPublicParameters) ciph;

		System.out.println("p: " + p.getP());
		System.out.println("q: " + p.getQ());
		System.out.println("n: " + p.getModulus());

		System.out
				.println("\nstarting the generation of the private e and d....");

		int strengthE = strength /3;
		KeyGenerationParameters keyGenParmPriv = new SRAPrivKeyGenerationParameters(
				random, strengthE, 25, p);

		SRAPrivateKeyGenerator genPriv = new SRAPrivateKeyGenerator();
		genPriv.init(keyGenParmPriv);
		AsymmetricCipherKeyPair keyPair = genPriv.generateKeyPair();
		System.out.println(".... done");

		System.out.println("e: "
				+ ((RSAPrivateCrtKeyParameters) keyPair.getPrivate())
						.getPublicExponent());
		System.out.println("d: geheim... sry.. kommt man nicht mehr ran :D");

		return keyPair;
	}

	/**
	 * Encrypts or decrypts a given message
	 * 
	 * @param forEncryption
	 *            says if the message should en- or decrypted
	 * @param message
	 *            the message
	 * @param param
	 *            the parameters for the en-/decryption (public/private key
	 *            pair)
	 * @return the new message
	 */
	private byte[] enDeCrypt(boolean forEncryption, byte[] message,
			CipherParameters param) {
		byte[] endecrM = null;
		AsymmetricBlockCipher rsaEng = new RSAEngine();
		rsaEng.init(forEncryption, param);
		try {
			endecrM = rsaEng.processBlock(message, 0, message.length);
		} catch (Exception e) {
			if (forEncryption)
				System.out.println("failed encryption - exception "
						+ e.toString());
			else
				System.out.println("failed decryption - exception "
						+ e.toString());
		}

		return endecrM;
	}

	/**
	 * Generates a key pair for RSA, encrypts a message, decrypts the message
	 * and compares the decrypted message to the original message.
	 */
	private void doMagic() {

		String inputData = "affex";

		/* Generate Key Pairs */
		AsymmetricCipherKeyPair keyPair = generateSRAKeyPair();

		System.out.println("\nEncryption"); 
		/*
		 * Encryption
		 */
		byte[] encrM = enDeCrypt(true, inputData.getBytes(),
				keyPair.getPublic());

		System.out.println("\nDecryption\n");
		/*
		 * Decryption
		 */
		byte[] decrM = enDeCrypt(false, encrM, keyPair.getPrivate());

		/*
		 * Check if decrypted message and original message are equal
		 */String

		sDecrM = new String(decrM);

		if (sDecrM.equals(inputData)) {
			System.out.println("Success");
		} else {
			System.out
					.println("No success, original message and decrypted message are not equal: ");
			System.out.println("original message: " + inputData);
			System.out.println("decrypted message: " + sDecrM);
		}

	}

	public static void main(String[] args) {
		SimpleSRATest sraTest = new SimpleSRATest();
		sraTest.doMagic();
	}

}
