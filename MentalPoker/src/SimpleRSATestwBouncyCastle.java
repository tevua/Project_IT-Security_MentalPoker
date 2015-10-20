
import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.engines.RSAEngine;

public class SimpleRSATestwBouncyCastle {
	
	/**
	 * Generate Key Pair for RSA Cipher
	 * @return key pair (public and private)
	 */
	private AsymmetricCipherKeyPair generateRSAKeyPair() 
	{
		BigInteger pubExp = new BigInteger("11", 16);
		SecureRandom random = new SecureRandom();  
		// strength has to be larger than 11 (low numbers here are the reason for the the error "input too large for RSA cipher.")
		int strength = 768; 
		// TODO: figure out what certainty does, which range of numbers is acceptable
		int certainty = 25; 
		
		KeyGenerationParameters keyGenParam = new RSAKeyGenerationParameters(pubExp, random, strength, certainty); 
		
		AsymmetricCipherKeyPairGenerator keyPairGen = new RSAKeyPairGenerator(); 
		keyPairGen.init(keyGenParam);
		
		return keyPairGen.generateKeyPair();
	}
	
	/**
	 * Encrypts or decrypts a given message
	 * @param forEncryption says if the message should en- or decrypted
	 * @param message the message
	 * @param param the parameters for the en-/decryption (public/private key pair)
	 * @return the new message
	 */
	private byte[] enDeCrypt(boolean forEncryption, byte[] message, CipherParameters param){
		byte[] endecrM = null;
		AsymmetricBlockCipher rsaEng = new RSAEngine();
		rsaEng.init(forEncryption, param); 
		try
        {
            endecrM = rsaEng.processBlock(message, 0, message.length);
        }
        catch (Exception e)
        {
            if (forEncryption)
            	System.out.println("failed encryption - exception " + e.toString());
            else
            	System.out.println("failed decryption - exception " + e.toString()); 
        }
		
		return endecrM;
	}
	
	/**
	 * Generates a key pair for RSA, encrypts a message, decrypts the message and compares the decrypted message to the original message.
	 */
	private void doMagic() {
		String inputData = "affex";
		
		/* Generate Key Pairs */
		AsymmetricCipherKeyPair keyPair = generateRSAKeyPair(); 
		
		/* Encryption */		
		byte[] encrM = enDeCrypt(true, inputData.getBytes() ,keyPair.getPublic());
		// TODO save as hex encoded encrypted string
		
		/* Decryption */
		byte[] decrM = enDeCrypt(false, encrM, keyPair.getPrivate()); 
		
		/* Check if decrypted message and original message are equal */
		String sDecrM = new String(decrM); 
		
		if (sDecrM.equals(inputData)) {
			System.out.println("Success"); 
		} else {
			System.out.println("No success, original message and decrypted message are not equal: ");
			System.out.println("original message: " + inputData); 
			System.out.println("decrypted message: " + sDecrM); 
		}
	}
	
	public static void main(String[] args) 
	{
		SimpleRSATestwBouncyCastle rsaTest = new SimpleRSATestwBouncyCastle();
		rsaTest.doMagic(); 
	}

}
