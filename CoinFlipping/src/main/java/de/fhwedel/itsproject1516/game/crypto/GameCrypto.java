package de.fhwedel.itsproject1516.game.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ProtocolException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRADecryptionKeySpec;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRAKeyGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class GameCrypto {

	private BigInteger p;
	// = new BigInteger(
	// "f2ac535b14a36b3707988f789d2ffd9d407e2ce1c387382678bf33932ebf708d067f2b2425704ddb7b708d51f94c7ee5868db43acc2649d611f71f5f4acd69b1b136fde10b5c4f70fe5f3a201aa2fdb79add4065d82bcc6460d8dac58906b4a856cb57317500c176ab8af9aa9e5f667c458c9ab70837f2f06bd1d2c2f1b7b50d",
	// 16);
	private BigInteger q;
	// = new BigInteger(
	// "c4f0f0d53e216ce3d8ccef361026b88bd07a14985a81d74772937f1b2be85e22b24dbf41f7e7a62232f7254f090b20f23d1dcb47a18e7438756e43c62b12b611c95f0cb0b7cc03dbd6c08601240857b09247b66fc420ab80e934a8e3bd17fafa233defbbad61cd27f98dda348f72a0f7d21309e16d1c69b235b8f149a3b637df",
	// 16);

	private Integer keySize;

	private KeyPair key;

	private String cipherAlg;

	public GameCrypto(Integer size, String oaepAlg) {
		this.keySize = size;
		Security.addProvider(new BouncyCastleProvider());

		switch (oaepAlg) {
		case "SHA1":
			cipherAlg = "SRA/NONE/OAEPPADDING";
			break;
		case "SHA256":
			cipherAlg = "SRA/NONE/OAEPWITHSHA256ANDMGF1PADDING";
			break;
		case "SHA512":
			cipherAlg = "SRA/NONE/OAEPWITHSHA512ANDMGF1PADDING";
			break;
		default:
			cipherAlg = null;
		}
	}

	public void generatePQ() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		// first get the sra key pair generator instance.
		KeyPairGenerator generator = KeyPairGenerator.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);

		// provide a bit-size for the key (1024-bit key in this example).
		generator.initialize(this.keySize, new SecureRandom());

		// generate the key pair.
		this.key = generator.generateKeyPair();

		// get a key factory instance for SRA
		KeyFactory factory = KeyFactory.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);

		// extract p and q. you have to use the private key for this, since only
		// the private key contains the information.
		// the key factory fetches the hidden information out of the private key
		// and fills a SRADecryptionKeySpec to provide the information.
		SRADecryptionKeySpec spec = factory.getKeySpec(this.key.getPrivate(), SRADecryptionKeySpec.class);

		// use getter-method on the spec to access p and q.
		this.p = spec.getQ();
		this.q = spec.getP();
	}

	public void setPQ(BigInteger primeP, BigInteger primeQ) {
		this.p = primeP;
		this.q = primeQ;
	}

	public void generateKeyPair()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

		// Key Generation with given p and q
		// create specifications for the key generation.
		SRAKeyGenParameterSpec specs = new SRAKeyGenParameterSpec(this.keySize, this.p, this.q);

		KeyPairGenerator generator = KeyPairGenerator.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);

		generator.initialize(specs);

		// generate a valid SRA key pair for the given p and q.
		this.key = generator.generateKeyPair();
	}

	public void setKeyPair(BigInteger e, BigInteger d) {
		BigInteger modulus = this.p.multiply(this.q);
		BCRSAPublicKey pubKey = new BCRSAPublicKey(new RSAKeyParameters(false, modulus, e));
		// BigInteger modulus, BigInteger publicExponent, BigInteger
		// privateExponent, BigInteger p, BigInteger q, BigInteger dP,
		// BigInteger dQ, BigInteger qInv);

		BigInteger one = BigInteger.valueOf(1);
		BigInteger dP = d.remainder(p.subtract(one));
		BigInteger dQ = d.remainder(q.subtract(one));
		BigInteger qInv = q.modInverse(p);
		BCRSAPrivateCrtKey privKey = new BCRSAPrivateCrtKey(
				new RSAPrivateCrtKeyParameters(modulus, e, d, p, q, dP, dQ, qInv));

		this.key = new KeyPair(pubKey, privKey);
	}

	public BigInteger getP() {
		return this.p;
	}

	public BigInteger getQ() {
		return this.q;
	}

	public byte[] decryptOAEP(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Cipher engine = Cipher.getInstance(cipherAlg, BouncyCastleProvider.PROVIDER_NAME);
		engine.init(Cipher.DECRYPT_MODE, key.getPrivate());
		byte[] decipher = engine.doFinal(data);

		return decipher;
	}

	public byte[] encryptOAEP(byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Security.addProvider(new BouncyCastleProvider());
		Cipher engine = Cipher.getInstance(cipherAlg, BouncyCastleProvider.PROVIDER_NAME);

		engine.init(Cipher.ENCRYPT_MODE, this.key.getPublic());
		byte[] cipher = engine.doFinal(data);

		return cipher;
	}

	public byte[] decrypt(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
		engine.init(Cipher.DECRYPT_MODE, key.getPrivate());
		byte[] decipher = engine.doFinal(data);

		return decipher;
	}

	public byte[] encrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);

		engine.init(Cipher.ENCRYPT_MODE, this.key.getPublic());
		byte[] cipher = engine.doFinal(data);

		return cipher;
	}

	public BigInteger getE() {
		return ((BCRSAPublicKey) this.key.getPublic()).getPublicExponent();
	}

	public BigInteger getD() {
		return ((BCRSAPrivateCrtKey) this.key.getPrivate()).getPrivateExponent();
	}

	public PrivateKey readPrivateKeyFromFile(String filename, String password)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
			IOException, UnrecoverableKeyException {
		File keystoreFile = new File(filename);

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(keystoreFile), password.toCharArray());

		String alias = ks.aliases().nextElement();

		Key key = ks.getKey(alias, password.toCharArray());
		return (PrivateKey) key;
	}

	public PublicKey readPublicKeyFromFile(String filename, String password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		File keystoreFile = new File(filename);

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(keystoreFile), password.toCharArray());

		String alias = (ks.aliases()).nextElement();

		Certificate cert = (Certificate) ks.getCertificate(alias);
		return cert.getPublicKey();
	}

	/**
	 * @param message
	 * @param privKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public byte[] sign(String message, PrivateKey privKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
		Security.addProvider(new BouncyCastleProvider());

		Signature signature = Signature.getInstance("SHA256with" + privKey.getAlgorithm(), "BC");
		signature.initSign(privKey);
		signature.update(message.getBytes());

		return signature.sign();

	}

	/**
	 * @param message
	 * @param sigBytes
	 * @param pubKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws SignatureException
	 */
	public boolean verify(String message, byte[] sigBytes, PublicKey pubKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException, SignatureException {
		Security.addProvider(new BouncyCastleProvider());

		Signature signer = Signature.getInstance("SHA256with" + pubKey.getAlgorithm(), "BC");
		signer.initVerify(pubKey);
		signer.update(message.getBytes());

		return signer.verify(sigBytes);
	}

	public void createSignature() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException,
			CertificateException, FileNotFoundException, IOException, InvalidKeyException, NoSuchProviderException,
			SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

		String KEY_ANNA = "client";
		String PASSWORD_KEY = "fhwedel";

		PrivateKey privKey = readPrivateKeyFromFile(KEY_ANNA, PASSWORD_KEY);

		PublicKey pubKey = readPublicKeyFromFile(KEY_ANNA, PASSWORD_KEY);

		String message = "Tail";
		byte[] signedData = sign(message, privKey);

		System.out.println(verify(message, signedData, pubKey));
	}

}
