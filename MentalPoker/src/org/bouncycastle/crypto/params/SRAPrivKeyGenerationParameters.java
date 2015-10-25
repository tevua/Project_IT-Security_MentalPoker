package org.bouncycastle.crypto.params;

import java.security.SecureRandom;

import org.bouncycastle.crypto.KeyGenerationParameters;

public class SRAPrivKeyGenerationParameters extends KeyGenerationParameters {

	private SRAPublicParameters sraPubParam;
	private int certainty; 
	
	public SRAPrivKeyGenerationParameters(SecureRandom random, int strengthE, int certainty, SRAPublicParameters sraPubParam) {
		super(random, strengthE);
		if (strengthE < 1) {
			throw new IllegalArgumentException("strength pub Exponent too small");
		}
		//TODO: check if e is too large...
		this.sraPubParam = sraPubParam; 
		this.certainty = certainty; 
	}
	
	public SRAPublicParameters getPubParameters() {
		return this.sraPubParam; 
	}

	public int getCertainty() {
		return certainty;
	}

}
