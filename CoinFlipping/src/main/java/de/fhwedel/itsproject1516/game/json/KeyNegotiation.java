package de.fhwedel.itsproject1516.game.json;

import java.math.BigInteger;

public class KeyNegotiation {

	/** the prime p */
	private BigInteger p;
	/** the prime q */
	private BigInteger q;

	/** the security identifier */
	// "id" : 0, "sraModulus" : "1024", "oaepAlg" : "SHA1"
	// "id" : 1, "sraModulus" : "2048", "oaepAlg" : "SHA1"
	// "id" : 2, "sraModulus" : "3072", "oaepAlg" : "SHA1"
	// "id" : 3, "sraModulus" : "4096", "oaepAlg" : "SHA1"
	// 10 : SRA1024/SHA256 (Payload <= 62Byte)
	// 11 : SRA2048/SHA256
	// 12 : SRA3072/SHA256
	// 13 : SRA4096/SHA256
	// 14 : SRA8192/SHA256
	// 15 : SRA16384/SHA256
	// 20 : SRA2048/SHA512
	// 21 : SRA3072/SHA512
	// 22 : SRA4096/SHA512
	// 23 : SRA8192/SHA512
	// 24 : SRA16384/SHA512
	private Integer sid;

	/** lists of available sids (should eventually be two) */
	private AvailableSids[] availableSids;

	/**
	 * Constructor
	 * 
	 * @param sids
	 *            the list of available security identifiers for the first
	 *            player
	 */
	public KeyNegotiation(Integer[] sids) {
		this.availableSids = new AvailableSids[1];
		this.availableSids[0] = new AvailableSids(sids);
	}

	/**
	 * Adds a list of available sids to the key negotiation.
	 * 
	 * @param sids
	 *            the list of available sids
	 */
	public void addAvailableSids(Integer[] sids) {
		int length = this.availableSids.length;
		AvailableSids[] newAvailableSids = new AvailableSids[length + 1];
		// copy old array to new array
		for (int i = 0; i < length; ++i) {
			newAvailableSids[i] = this.availableSids[i];
		}
		newAvailableSids[length] = new AvailableSids(sids);
		this.availableSids = newAvailableSids;
	}

	/**
	 * Adds the chosen common sid to the key negotiation.
	 * 
	 * @param s
	 *            chosen, common version
	 */
	public void addCommonSid(Integer s) {
		this.sid = s;
	}

	/**
	 * Adds the primes p and q.
	 * 
	 * @param primeP
	 *            the prime p
	 * @param primeQ
	 *            the prime q
	 */
	public void addPandQ(BigInteger primeP, BigInteger primeQ) {
		this.p = primeP;
		this.q = primeQ;
	}

	/**
	 * Checks that the current key negotiation equals the previous key
	 * negotiation.
	 * 
	 * @param prev
	 *            the previous key negotiation
	 * @return true if the previous equals the current key negotiation
	 */
	public boolean checkPrev(KeyNegotiation prev) {
		boolean result = true;

		// elements in the array should be the same as they were in the previous
		// version (and the order should not change)
		int lengthPrev = prev.getAvailableSids().length;
		for (int i = 0; i < lengthPrev; ++i) {
			AvailableSids as1 = this.availableSids[i];
			result = result && (as1.checkPrev(prev.getAvailableSids()[i]));
		}

		if (prev.getSid() != null) {
			result = result && prev.getSid().equals(this.getSid());
		}

		if (prev.getP() != null) {
			result = result && prev.getP().equals(this.getP());
		}

		if (prev.getQ() != null) {
			result = result && prev.getQ().equals(this.getQ());
		}

		return result;
	}

	/**
	 * Checks that the key negotiation is valid.
	 * 
	 * @param pid
	 *            the protocol identifier
	 * @return true if the key negotiation is valid
	 */
	public boolean checkValid(Integer pid) {
		switch (pid) {
		case 0:
		case 1:
			return true;
		case 2:
			// check if valid sids in the list of availableSids
			return this.p == null && this.q == null && this.sid == null && this.availableSids != null
					&& this.availableSids[0] != null && validSid(this.availableSids[0].getSids());
		default:
			return this.p != null && this.q != null && this.sid != null && this.availableSids.length == 2
					&& validSid(this.availableSids[1].getSids()) && arrayContainsSid(this.availableSids[0].getSids())
					&& arrayContainsSid(this.availableSids[1].getSids());

		}
	}

	/**
	 * Returns true if a given sid is a valid sid.
	 * 
	 * @param sid
	 *            the sid
	 * @return true if the sid is valid
	 */
	private boolean validSid(Integer sid) {
		return (sid == 0 | sid == 1 || sid == 2 || sid == 3 || sid == 4 || sid == 10 || sid == 11 || sid == 12
				|| sid == 13 || sid == 14 || sid == 15 || sid == 20 || sid == 21 || sid == 22 || sid == 23
				|| sid == 24);
	}

	/**
	 * Returns true if a given array of sids contains only valid sids
	 * 
	 * @param sids
	 *            array of sids
	 * @return true if the array only contains valid sids
	 */
	private boolean validSid(Integer[] sids) {
		boolean result = true;
		for (Integer s : sids) {
			result = result && validSid(s);
		}
		return result;
	}

	/**
	 * Checks if a given array contains the chosen security identifier.
	 * 
	 * @param a
	 *            the array
	 * @return true if the array contains this.version
	 */
	private boolean arrayContainsSid(Integer[] a) {
		boolean found = false;
		for (Integer i : a) {
			if (i.equals(this.sid)) {
				found = true;
			}
		}
		return found;
	}

	/**
	 * Computes the common security identifier.
	 * 
	 * @return the common security identifier (or null if none can be found)
	 */
	public Integer computeCommonSid() {
		if (this.availableSids.length == 2) {
			Integer[] v1 = this.availableSids[0].getSids();
			Integer[] v2 = this.availableSids[1].getSids();
			Integer common = null;
			if (v1 != null && v2 != null) {
				for (int i = 0; i < v1.length; i++) {
					for (int j = 0; j < v2.length; j++) {
						if (v1[i].equals(v2[j])) {
							if (common != null) {
								if (common > v1[i]) {
									common = v1[i];
								}
							} else {
								common = v1[i];
							}
						}
					}
				}
			}
			return common;
		} else {
			return null;
		}
	}

	/**
	 * Returns the availaible sids
	 * 
	 * @return
	 */
	private AvailableSids[] getAvailableSids() {
		return this.availableSids;
	}

	/**
	 * Returns the prime p.
	 * 
	 * @return the prime p
	 */
	public BigInteger getP() {
		return this.p;
	}

	/**
	 * Returns the prime q.
	 * 
	 * @return prime q
	 */
	public BigInteger getQ() {
		return this.q;
	}

	/**
	 * Returns the common sid.
	 * 
	 * @return the common sid
	 */
	public Integer getSid() {
		return this.sid;
	}

	public String getOAEPAlg() {
		switch (this.sid) {
		case 0:
		case 1:
		case 2:
		case 3:
			return "SHA1";
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
			return "SHA256";
		case 20:
		case 21:
		case 22:
		case 23:
		case 24:
			return "SHA512";
		default:
			return null;
		}
	}

	/**
	 * Returns the chosen key size (derived from the sid)
	 * 
	 * @return key size
	 */
	public Integer getKeySize() {

		switch (this.sid) {
		case 0:
		case 10:
			return 1024;
		case 1:
		case 11:
		case 20:
			return 2048;
		case 2:
		case 12:
		case 21:
			return 3072;
		case 3:
		case 13:
		case 22:
			return 4096;
		case 14:
		case 23:
			return 8192;
		case 15:
		case 24:
			return 16384;
		default:
			return null;
		}
	}

}
