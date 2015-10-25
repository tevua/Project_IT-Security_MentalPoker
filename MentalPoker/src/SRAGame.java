import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.SRAPrivateKeyGenerator;
import org.bouncycastle.crypto.generators.SRAPublicKeyGenerator;
import org.bouncycastle.crypto.params.SRAPrivKeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPubKeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPublicParameters;
import org.bouncycastle.util.encoders.Hex;

/**
 * The cryptography part of the game "Coin Flipping": encryption, decryption and
 * generation of keys.
 * 
 * @author tevua
 * @version 1.0
 * 
 */
public class SRAGame {

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
	 * Decrypts a given String that is in Hex representation.
	 * 
	 * @param message
	 *            the String that is currently encrypted
	 * @param key
	 *            the private key of RSA to decrypt it
	 * @return the decrypted String
	 */
	protected String decrypt(String message, CipherParameters key) {
		byte[] decr = enDeCrypt(false, Hex.decode(message), key);
		return new String(Hex.encode(decr));
	}

	/**
	 * Encrypts a given String in Hex
	 * 
	 * @param message
	 *            the String that is supposed to be encrypted.
	 * @param key
	 *            the public key to encrypt it
	 * @return the encrypted message as a string
	 */
	protected String encrypt(String message, CipherParameters key) {
		byte[] encr = enDeCrypt(true, Hex.decode(message), key);
		return new String(Hex.encode(encr));
	}

	/**
	 * Generates the private part of the SRA algorithm (essentially e and d).
	 * 
	 * @param strengthE
	 *            the strength (length in bits) e is supposed to have
	 * @param certainty
	 *            how certain e is a prime
	 * @param p
	 *            the public parameters (p and q)
	 * @return the public and private key of RSA (which are both private in SRA)
	 */
	protected AsymmetricCipherKeyPair createPrivate(int strengthE, int certainty,
			SRAPublicParameters p) {

		KeyGenerationParameters keyGenParmPriv = new SRAPrivKeyGenerationParameters(
				new SecureRandom(), strengthE, certainty, p);

		SRAPrivateKeyGenerator genPriv = new SRAPrivateKeyGenerator();
		genPriv.init(keyGenParmPriv);
		return genPriv.generateKeyPair();
	}

	/**
	 * Generates the public part of the SRA (p and q and n = pq).
	 * 
	 * @param strength
	 *            the strength of n = pq (length in bits)
	 * @param certainty
	 *            how certain p and q are primes
	 * @return the public part of the SRA key
	 */
	protected SRAPublicParameters createPublic(int strength, int certainty) {
		// Erstellen des öffentlichen Teil des Schlüssel

		KeyGenerationParameters keyGenParm = new SRAPubKeyGenerationParameters(
				new SecureRandom(), strength, certainty);

		SRAPublicKeyGenerator genPub = new SRAPublicKeyGenerator();
		genPub.init(keyGenParm);

		return genPub.generateKeyPair();
	}
}
