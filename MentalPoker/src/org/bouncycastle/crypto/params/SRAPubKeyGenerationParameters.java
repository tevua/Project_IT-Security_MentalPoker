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

	public SRAPubKeyGenerationParameters(SecureRandom random, int strength,
			int certainty) {
		super(random, strength);

		if (strength < 12) {
			throw new IllegalArgumentException("key strength too small");
		}
		this.certainty = certainty;
	}

	public int getCertainty() {
		return certainty;
	}

}
