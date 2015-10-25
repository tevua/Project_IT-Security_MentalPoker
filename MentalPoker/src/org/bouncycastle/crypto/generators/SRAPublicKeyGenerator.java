package org.bouncycastle.crypto.generators;

import java.math.BigInteger;

import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPubKeyGenerationParameters;
import org.bouncycastle.crypto.params.SRAPublicParameters;
import org.bouncycastle.math.ec.WNafUtil;

/**
 * Generator for the public part of the key of SRA (p and q and n = pq).
 * 
 * Format and many code fragments copied from
 * org.bouncycastle.crypto.generators.RSAKeyPairGenerator.
 * 
 * The differences are that only p and q and n = pq are generated (as those are
 * public in SRA) and e, the public exponent is not known and is not important
 * in the generation process.
 * 
 * @author tevua
 * @version 1.0
 */
public class SRAPublicKeyGenerator {

	/* Parameters for the key generation include strength and certainty */
	private SRAPubKeyGenerationParameters param;

	public void init(KeyGenerationParameters param) {
		this.param = (SRAPubKeyGenerationParameters) param;
	}

	public SRAPublicParameters generateKeyPair() {
		SRAPublicParameters result = null;
		boolean done = false;

		//
		// p and q values should have a length of half the strength in bits
		//
		int strength = param.getStrength();
		int pbitlength = (strength + 1) / 2;
		int qbitlength = strength - pbitlength;
		int mindiffbits = strength / 3;
		int minWeight = strength >> 2;

		while (!done) {
			BigInteger p, q, n, gcd;

			// TODO Consider generating safe primes for p, q (see
			// DHParametersHelper.generateSafePrimes)
			// (then p-1 and q-1 will not consist of only small factors - see
			// "Pollard's algorithm")

			p = chooseRandomPrime(pbitlength);

			//
			// generate a modulus of the required length
			//
			for (;;) {
				q = chooseRandomPrime(qbitlength);

				// p and q should not be too close together (or equal!)

				// TODO Since any common factors of (p-1) and (q-1) are present
				// in
				// the factorisation of p*q-1, it is recommended that (p-1)
				// and (q-1) have only very small common factors, if any besides
				// the necessary 2.
				BigInteger diff = q.subtract(p).abs();
				if (diff.bitLength() < mindiffbits) {
					continue;
				}

				//
				// calculate the modulus
				//
				n = p.multiply(q);

				if (n.bitLength() != strength) {
					//
					// if we get here our primes aren't big enough, make the
					// largest
					// of the two p and try again
					//
					p = p.max(q);
					continue;
				}

				/*
				 * Require a minimum weight of the NAF representation, since
				 * low-weight composites may be weak against a version of the
				 * number-field-sieve for factoring.
				 * 
				 * See "The number field sieve for integers of low weight",
				 * Oliver Schirokauer.
				 */
				if (WNafUtil.getNafWeight(n) < minWeight) {
					p = chooseRandomPrime(pbitlength);
					continue;
				}

				break;
			}

			// TODO replace gcd ...
			if (p.compareTo(q) < 0) {
				gcd = p;
				p = q;
				q = gcd;
			}

			done = true;

			result = new SRAPublicParameters(p, q, n);
		}

		return result;
	}

	/**
	 * Choose a random prime value for use with RSA
	 * 
	 * Copied from org.bouncycastle.crypto.generators.RSAKeyPairGenerator and
	 * modified in a way that e is not important anymore.
	 * 
	 * @param bitlength
	 *            the bit-length of the returned prime
	 * @return a prime p
	 */
	protected BigInteger chooseRandomPrime(int bitlength) {
		for (;;) {
			BigInteger p = new BigInteger(bitlength, 1, param.getRandom());

			if (!p.isProbablePrime(param.getCertainty())) {
				continue;
			}

			// gcd(e, p − 1) = 1 and gcd(e, q − 1) = 1
			// when e is prime and greater than 2 it is faster to check
			// p mod e =/= 1 and q mod e =/= 1
			//reason for: 
            //if (p.mod(e).equals(ONE))
            //{
            //    continue;
            //}
			
			// The mathematical requirement on e is that gcd(e, φ(n)) = 1,
			// since otherwise e will not have a multiplicative inverse mod
			// φ(n).
			// Since n = p × q, this requirement is equivalent to the two
			// requirements gcd(e, φ(p)) = 1 and gcd(e, φ(q)) = 1. In other
			// words, we want gcd(e, p − 1) = 1 and gcd(e, q − 1) = 1.
			// reason for :
			// if (!e.gcd(p.subtract(ONE)).equals(ONE))
			// {
			// continue;
			// }

			return p;
		}
	}

}
