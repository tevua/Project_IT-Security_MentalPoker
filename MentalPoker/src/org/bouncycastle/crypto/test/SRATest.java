package org.bouncycastle.crypto.test;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.generators.SRAPrivateKeyGenerator;
import org.bouncycastle.crypto.generators.SRAPublicKeyGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.params.SRAPrivKeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPubKeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPublicParameters;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.test.SimpleTest;
import org.bouncycastle.crypto.test.RSATest;

/***
 * Testing the SRA implementation. Test very similiar to/copied from
 * org.bouncycastle.crypto.test.RSATest, as the SRA implementation only differs
 * in the key generation. For encryption and decryption the RSA implementation
 * is used.
 * 
 * @author tevua
 */
public class SRATest extends RSATest {

	/**
	 * Creates the public key part of the SRA key. 
	 * @param strength the strength of n = pq 
	 * @param certainty how certain p and q are prime
	 * @return
	 */
	private SRAPublicParameters createPublicKeys(int strength, int certainty) {
		// Erstellen des öffentlichen Teil des Schlüssel

		KeyGenerationParameters keyGenParm = new SRAPubKeyGenerationParameters(
				new SecureRandom(), strength, certainty);

		SRAPublicKeyGenerator genPub = new SRAPublicKeyGenerator();
		genPub.init(keyGenParm);

		return (SRAPublicParameters) genPub.generateKeyPair();
	}

	/**
	 * Creates public and private keys for SRA.
	 * 
	 * @param strength
	 *            the strength of n = pq
	 * @param certainty
	 *            how certain p and q are prime
	 * @return private keys for SRA (which are public and private keys for RSA)
	 */
	private AsymmetricCipherKeyPair createKeys(int strength, int certainty) {

		SRAPublicParameters p = createPublicKeys(strength, certainty);

		// starting the generation of the private e and d....

		KeyGenerationParameters keyGenParmPriv = new SRAPrivKeyGenerationParameters(
				new SecureRandom(), strength, certainty, p);

		SRAPrivateKeyGenerator genPriv = new SRAPrivateKeyGenerator();
		genPriv.init(keyGenParmPriv);

		return genPriv.generateKeyPair();
	}

	@Override
	public String getName() {
		return "SRA";
	}

	@Override
	public void performTest() {

		byte[] data = Hex.decode(input);

		AsymmetricBlockCipher eng = new RSAEngine();

		//
		// key generation test
		//

		AsymmetricCipherKeyPair pair = createKeys(768, 25);

		eng = new RSAEngine();

		// test if the generated keys have the correct length
		if (((RSAKeyParameters) pair.getPublic()).getModulus().bitLength() < 768) {
			fail("failed key generation (768) length test");
		}

		eng.init(true, pair.getPublic());

		// try to encrypt something
		try {
			data = eng.processBlock(data, 0, data.length);
		} catch (Exception e) {
			fail("failed - exception " + e.toString(), e);
		}

		eng.init(false, pair.getPrivate());

		try {
			data = eng.processBlock(data, 0, data.length);
		} catch (Exception e) {
			fail("failed - exception " + e.toString(), e);
		}

		if (!input.equals(new String(Hex.encode(data)))) {
			fail("failed key generation (768) Test");
		}

		pair = createKeys(1024, 25);

		eng.init(true, pair.getPublic());

		if (((RSAKeyParameters) pair.getPublic()).getModulus().bitLength() < 1024) {
			fail("failed key generation (1024) length test");
		}

		try {
			data = eng.processBlock(data, 0, data.length);
		} catch (Exception e) {
			fail("failed - exception " + e.toString(), e);
		}

		eng.init(false, pair.getPrivate());

		try {
			data = eng.processBlock(data, 0, data.length);
		} catch (Exception e) {
			fail("failed - exception " + e.toString(), e);
		}

		if (!input.equals(new String(Hex.encode(data)))) {
			fail("failed key generation (1024) test");
		}

		KeyGenerationParameters keyGenParmPriv = new SRAPrivKeyGenerationParameters(
				new SecureRandom(), 16, 25, createPublicKeys(16, 25));

		SRAPrivateKeyGenerator genPriv = new SRAPrivateKeyGenerator();
		genPriv.init(keyGenParmPriv);

		for (int i = 0; i < 100; ++i) {
			pair = genPriv.generateKeyPair();
			RSAPrivateCrtKeyParameters privKey = (RSAPrivateCrtKeyParameters) pair
					.getPrivate();
			BigInteger pqDiff = privKey.getP().subtract(privKey.getQ()).abs();

			if (pqDiff.bitLength() < 5) {
				fail("P and Q too close in RSA key pair");
			}
		}

		try {
			new RSAEngine().processBlock(new byte[] { 1 }, 0, 1);
			fail("failed initialisation check");
		} catch (IllegalStateException e) {
			// expected
		}

	}

	public static void main(String[] args) {
		runTest(new SRATest());
	}

}
