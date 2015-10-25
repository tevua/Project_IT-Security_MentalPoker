package org.bouncycastle.crypto.params;

import java.security.SecureRandom;

import org.bouncycastle.crypto.KeyGenerationParameters;

/**
 * Parameters for the generation of the public part of the SRA algorithm.
 * 
 * @author tevua
 * 
 */
public class SRAPubKeyGenerationParameters extends KeyGenerationParameters {

	/* how certain the numbers are prime */
	private int certainty;

	/**
	 * Constructor
	 * 
	 * @param random
	 * @param strength
	 *            strength of n = pq (bitLength)
	 * @param certainty
	 *            how certain p and q are primes
	 */
	public SRAPubKeyGenerationParameters(SecureRandom random, int strength,
			int certainty) {
		super(random, strength);

		if (strength < 12) {
			throw new IllegalArgumentException("key strength too small");
		}
		this.certainty = certainty;
	}

	/**
	 * Returns the certainty
	 * 
	 * @return how certain p and q are supposed to be primes
	 */
	public int getCertainty() {
		return certainty;
	}

}
