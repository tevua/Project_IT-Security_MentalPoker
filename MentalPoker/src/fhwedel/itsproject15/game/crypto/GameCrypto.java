package fhwedel.itsproject15.game.crypto;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRAKeyGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class GameCrypto {

	static BigInteger p = new BigInteger(
			"f2ac535b14a36b3707988f789d2ffd9d407e2ce1c387382678bf33932ebf708d067f2b2425704ddb7b708d51f94c7ee5868db43acc2649d611f71f5f4acd69b1b136fde10b5c4f70fe5f3a201aa2fdb79add4065d82bcc6460d8dac58906b4a856cb57317500c176ab8af9aa9e5f667c458c9ab70837f2f06bd1d2c2f1b7b50d",
			16);
	static BigInteger q = new BigInteger(
			"c4f0f0d53e216ce3d8ccef361026b88bd07a14985a81d74772937f1b2be85e22b24dbf41f7e7a62232f7254f090b20f23d1dcb47a18e7438756e43c62b12b611c95f0cb0b7cc03dbd6c08601240857b09247b66fc420ab80e934a8e3bd17fafa233defbbad61cd27f98dda348f72a0f7d21309e16d1c69b235b8f149a3b637df",
			16);

	private Integer keySize;

	private KeyPair key;

	public GameCrypto(Integer size) {
		this.keySize = size;
	}

	public void generatePQ() {
		// TODO
	}

	public void setPQ(BigInteger primeP, BigInteger primeQ) {
		this.p = primeP;
		this.q = primeQ;
	}

	public void generateKeyPair()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		SRAKeyGenParameterSpec sraKeyGenParameterSpec = new SRAKeyGenParameterSpec(keySize, p, q);

		KeyPairGenerator generator;
		generator = KeyPairGenerator.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
		generator.initialize(sraKeyGenParameterSpec);
		this.key = generator.generateKeyPair();
	}
	
	public void setKeyPair(BigInteger e, BigInteger d) {
		BigInteger modulus = this.p.multiply(this.q); 
		BCRSAPublicKey pubKey = new BCRSAPublicKey(new RSAKeyParameters(false, modulus, e)); 
		//BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent, BigInteger p, BigInteger q, BigInteger dP, BigInteger dQ, BigInteger qInv);
	
		BigInteger one = BigInteger.valueOf(1);
		BigInteger dP = d.remainder(p.subtract(one)); 
		BigInteger dQ = d.remainder(q.subtract(one)); 
		BigInteger qInv = q.modInverse(p); 
		BCRSAPrivateCrtKey privKey = new BCRSAPrivateCrtKey(new RSAPrivateCrtKeyParameters(modulus, e, d, p, q, dP, dQ, qInv)); 
		
		this.key = new KeyPair(pubKey, privKey); 
	}
	
	public BigInteger getP() {
		return this.p;
	}

	public BigInteger getQ() {
		return this.q;
	}

	public byte[] decrypt(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
		engine.init(Cipher.DECRYPT_MODE, key.getPrivate());
		byte[] decipher = engine.doFinal(data);

		return decipher;
	}

	public byte[] encrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchProviderException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);

		engine.init(Cipher.ENCRYPT_MODE, this.key.getPublic());
		byte[] cipher = engine.doFinal(data);

		return cipher;
	}
	
	public BigInteger getE() {
		return ((BCRSAPublicKey)this.key.getPublic()).getPublicExponent();
	}
	
	public BigInteger getD() {
		return ((BCRSAPrivateCrtKey)this.key.getPrivate()).getPrivateExponent();
	}
}
