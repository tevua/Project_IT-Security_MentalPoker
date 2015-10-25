package org.bouncycastle.crypto.params;

import java.math.BigInteger;

import org.bouncycastle.crypto.CipherParameters;

/**
 * A holding class for the public parameters of SRA (which are p and q). 
 * @author tevua
 *
 */

public class SRAPublicParameters implements CipherParameters {
	
	/* first big prime needed for SRA */
	private BigInteger p; 
	/* second big prime needed for SRA */
	private BigInteger q; 
	/* product of p and q */
	private BigInteger modulus; 
	
	/**
	 * 
	 * @param p a big prime
	 * @param q another big prime
	 */
	public SRAPublicParameters(BigInteger p, BigInteger q, BigInteger n) {
		this.p = p; 
		this.q = q; 
		this.modulus = n; 
	}
	
	public BigInteger getP() {
		return this.p; 
	}
	
	public BigInteger getQ() {
		return q; 
	}
	
	public BigInteger getModulus() {
		return modulus; 
	}
}
