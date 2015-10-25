package org.bouncycastle.crypto.generators;

import java.math.BigInteger;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.params.SRAPrivKeyGenerationParameters;

/**
 * Generator for the private part of the key of SRA (e and d).
 * 
 * Format and many code fragments copied from
 * org.bouncycastle.crypto.generators.RSAKeyPairGenerator.
 * 
 * The differences are that p and q are already generated and instead e needs to
 * be generated.
 * 
 * @author tevua
 * @version 1.0
 */
public class SRAPrivateKeyGenerator implements AsymmetricCipherKeyPairGenerator {
	private static final BigInteger ONE = BigInteger.valueOf(1);

	/**
	 * Parameters for the generation.
	 */
	private SRAPrivKeyGenerationParameters param;

	/**
	 * Initialisation
	 * 
	 * @param the
	 *            parameters needed for the generation
	 */
	public void init(KeyGenerationParameters param) {
		this.param = (SRAPrivKeyGenerationParameters) param;
	}

	/**
	 * Generates the key pair
	 * 
	 * @return the generated private key pair (public and private key of RSA)
	 */
	public AsymmetricCipherKeyPair generateKeyPair() {
		AsymmetricCipherKeyPair result = null;
		boolean done = false;

		BigInteger n = param.getPubParameters().getModulus();
		BigInteger p = param.getPubParameters().getP();
		BigInteger q = param.getPubParameters().getQ();
		//
		// p and q values should have a length of half the strength in bits
		//
		int strength = n.bitLength();

		// d lower bound is 2^(strength / 2)
		BigInteger dLowerBound = BigInteger.valueOf(2).pow(strength / 2);

		while (!done) {
			BigInteger d, e, pSub1, qSub1, phi, gcd, lcm;

			// Compute φ(n) = φ(p)φ(q) = (p − 1)(q − 1) = n - (p + q -1)
			pSub1 = p.subtract(ONE);
			qSub1 = q.subtract(ONE);
			phi = pSub1.multiply(qSub1);

			int strengthE = param.getStrength();
			if (strengthE >= phi.bitLength()) {
				strengthE = phi.bitLength() - 1;
			}

			e = chooseRandomE(strengthE, phi, p, q);

			//
			// calculate the "private" exponent d
			//

			// choose d matching de ≡ 1 (mod λ) with λ = lcm(p − 1, q − 1)
			gcd = pSub1.gcd(qSub1);
			lcm = pSub1.divide(gcd).multiply(qSub1);

			d = e.modInverse(lcm);

			if (d.compareTo(dLowerBound) <= 0) {
				continue;
			} else {
				done = true;
			}

			//
			// calculate the CRT factors
			//
			BigInteger dP, dQ, qInv;

			dP = d.remainder(pSub1);
			dQ = d.remainder(qSub1);
			qInv = q.modInverse(p);

			result = new AsymmetricCipherKeyPair(new RSAKeyParameters(false, n,
					e), new RSAPrivateCrtKeyParameters(n, e, d, p, q, dP, dQ,
					qInv));
		}

		return result;
	}

	/**
	 * Choose a random prime value for use as the Public Exponent in e such that
	 * 1 < e < φ(n) and gcd(e, φ(n)) = 1 and e relatively prime to p − 1 and q −
	 * 1
	 * 
	 * This function is based on
	 * RSAKeyPairGenerator.chooseRandomPrime(bitlength,p,q);
	 * 
	 * @param bitlength
	 *            the bit-length of the returned prime
	 * @param p
	 *            the first prime of RSA
	 * @param q
	 *            the second prime of RSA
	 * @return a prime p, with (p-1) relatively prime to e
	 */
	protected BigInteger chooseRandomE(int bitlength, BigInteger phi,
			BigInteger p, BigInteger q) {

		for (;;) {
			BigInteger e = new BigInteger(bitlength, 1, param.getRandom());

			if (!(e.compareTo(phi) < 0)) {
				continue;
			}

			if (!e.isProbablePrime(param.getCertainty())) {
				continue;
			}

			// TODO think if you still need to check if e > 2
			if (p.mod(e).equals(ONE)) {
				continue;
			}

			if (q.mod(e).equals(ONE)) {
				continue;
			}

			if (!e.gcd(phi).equals(ONE)) {
				continue;
			}

			return e;
		}
	}
}
