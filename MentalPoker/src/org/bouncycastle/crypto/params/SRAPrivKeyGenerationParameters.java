package org.bouncycastle.crypto.params;

import java.security.SecureRandom;

import org.bouncycastle.crypto.KeyGenerationParameters;

/**
 * Holding class for the parameters for the generation of the private parameters
 * of SRA.
 * 
 * @author tevua
 * @version 1.0
 */
public class SRAPrivKeyGenerationParameters extends KeyGenerationParameters {

	// public parameters (p and q)
	private SRAPublicParameters sraPubParam;
	// how certain e is a prime
	private int certainty;

	/**
	 * Constructor.
	 * 
	 * @param random
	 * @param strengthE
	 *            the strength (bitLength) of e .. if it is too large it is just
	 *            set to the maximum it can be
	 * @param certainty
	 *            how certain e is a prime
	 * @param sraPubParam
	 *            the public parameters (p and q)
	 */
	public SRAPrivKeyGenerationParameters(SecureRandom random, int strengthE,
			int certainty, SRAPublicParameters sraPubParam) {
		super(random, strengthE);
		if (strengthE < 1) {
			throw new IllegalArgumentException(
					"strength pub Exponent too small");
		}
		this.sraPubParam = sraPubParam;
		this.certainty = certainty;
	}

	/**
	 * Returns the public parameters
	 * 
	 * @return the public parameter
	 */
	public SRAPublicParameters getPubParameters() {
		return this.sraPubParam;
	}

	/**
	 * Returns the certainty.
	 * 
	 * @return how certain e is supposed to be prime
	 */
	public int getCertainty() {
		return certainty;
	}

}
