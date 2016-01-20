package de.fhwedel.itsproject1516.game.json;

import java.math.BigInteger;

/**
 * The representation of the JSON-message for the coin flipping game.
 * 
 * TODO: check previous payload equals current and that payload is valid
 * 
 * @author tevua
 * @version 1.0
 */
public class CoinFlippingMessage extends GameMessage {

	/** the key negotiation */
	private KeyNegotiation keyNegotiation;
	/** the payload */
	private Payload payload;

	/**
	 * The constructor.
	 * 
	 * @param pid
	 *            the protocol id
	 * @param s
	 *            the status id (0 if no error)
	 * @param m
	 *            the status message ("OK" if no error)
	 * @param versions
	 *            the available protocol versions of the start-player
	 */
	public CoinFlippingMessage(int pid, int s, String m, String[] versions) {
		super(pid, s, m, new ProtocolNegotiation(new AvailableVersion[] { new AvailableVersion(versions) }));
	}

	/**
	 * Checks that current message equals previous message.
	 * 
	 * @param prev
	 *            the previous message
	 * @return true if current message equals previous
	 */
	public boolean checkPrev(CoinFlippingMessage prev) {
		boolean result = true;
		// can only compare to previous protocol if id is at least 1
		if (this.protocolId > 0) {
			// difference to previous protocol id should be 1
			int pidPrev = prev.getProtocolId();
			result = result && ((pidPrev + 1) == this.protocolId);

			// check previous protocol negotiation equals current
			result = result && ((this.protocolNegotiation != null)
					&& (this.protocolNegotiation.checkPrev(prev.getProtocolNegotiation())));
			if (result && this.protocolId > 2) {
				// check previous keyNegotiation equals current
				result = this.keyNegotiation != null && this.keyNegotiation.checkPrev(prev.keyNegotiation);
				if (result && this.protocolId > 4) {
					result = this.payload != null && this.payload.checkPrev(prev.payload);
					// check previous payload equals current
					// TODO
				}
			}

		}
		return result;
	}

	/**
	 * Checks that the key negotiaton is valid
	 * 
	 * @return true if key negotiation is valid
	 */
	public boolean checkValidKeyNegotiation() {
		return this.keyNegotiation.checkValid(this.protocolId);
	}

	/**
	 * Checks that payload is valid.
	 * 
	 * @return true if payload is valid
	 */
	public boolean checkProperPayload() {
		// TODO
		return true;
	}

	/**
	 * Calculates the common security identifier.
	 * 
	 * @return the common sid
	 */
	public Integer calculateCommonSid() {
		return this.keyNegotiation.computeCommonSid();
	}

	/**
	 * Adds another list of availalbe sids
	 * 
	 * @param sids
	 *            the list of available sids
	 */
	public void addAvailableSids(Integer[] sids) {
		if (keyNegotiation == null) {
			this.keyNegotiation = new KeyNegotiation(sids);
		} else {
			this.keyNegotiation.addAvailableSids(sids);
		}
	}

	/**
	 * Adds a common sid
	 * 
	 * @param s
	 *            the common sid
	 */
	public void addCommonSid(Integer s) {
		this.keyNegotiation.addCommonSid(s);
	}

	/**
	 * Adds the initial payload (which will be extendend).
	 * 
	 * @param initialCoin
	 *            the initialCoin (in plaintext)
	 * @param encryptedCoin
	 *            the initital coin as encrypted hex string
	 */
	public void addPayload(String[] initialCoin, String[] encryptedCoin) {
		this.payload = new Payload(initialCoin, encryptedCoin);
	}

	/**
	 * Adds p and q.
	 * 
	 * @param p
	 *            prime p
	 * @param q
	 *            prime q
	 */
	public void addPandQ(BigInteger p, BigInteger q) {
		this.keyNegotiation.addPandQ(p, q);
	}

	/**
	 * Adds the chosen coin side
	 * 
	 * @param desired
	 *            the desired coin side (in plain text)
	 * @param encrypted
	 *            one of the encrypted coin sids in hex string (twice encrypted
	 *            now)
	 */
	public void addChosenCoinSide(String desired, String encrypted) {
		this.payload.addChosenCoinSide(desired, encrypted);

	}

	/**
	 * Adds the decrypted chosen coin side.
	 * 
	 * @param side
	 *            decrypted (but still once encrypted) coin side in hex string
	 */
	public void addDeChosenCoinSide(String side) {
		this.payload.addDeChosenCoin(side);

	}

	/**
	 * Adds the key of player a
	 * 
	 * @param e
	 *            the public exponent of the key
	 * @param d
	 *            the private exponent of the key
	 */
	public void addKeyA(BigInteger e, BigInteger d) {
		this.payload.addKeyA(new BigInteger[] { e, d });
	}

	/**
	 * Adds the key player b.
	 * 
	 * @param e
	 *            the public exponent of the key
	 * @param d
	 *            the private exponent of the key
	 */
	public void addKeyB(BigInteger e, BigInteger d) {
		this.payload.addKeyB(new BigInteger[] { e, d });
	}

	/**
	 * Returns the key size (derived from the common sid).
	 * 
	 * @return the key size
	 */
	public Integer getKeySize() {
		return this.keyNegotiation.getKeySize();
	}

	/**
	 * Returns the prime p.
	 * 
	 * @return the prime p
	 */
	public BigInteger getP() {
		return this.keyNegotiation.getP();
	}

	/**
	 * Returns the prime q.
	 * 
	 * @return the prime q
	 */
	public BigInteger getQ() {
		return this.keyNegotiation.getQ();
	}

	/**
	 * Returns the initial coin as plain text
	 * 
	 * @return the initial coin
	 */
	public String[] getInitialCoin() {
		return this.payload.getInitialCoin();
	}

	/**
	 * Returns the initial, but encrypted coin.
	 * 
	 * @return the encrypted coin
	 */
	public String[] getEncryptedCoin() {
		return this.payload.getEncryptedCoin();
	}

	/**
	 * Returns the twice encrypted chosen coin side.
	 * 
	 * @return encrypted chosen coin sid
	 */
	public String getEnChosenSide() {
		return this.payload.getEnChosenCoin();
	}

	/**
	 * Returns the once decrypted, but still once encypted chosen coin side.
	 * 
	 * @return once decrypted chosen coin sid
	 */
	public String getDeChosenSide() {
		return this.payload.getDeChosenCoin();
	}

	/**
	 * Returns the desired coin side (in plain text).
	 * 
	 * @return the desired coin side
	 */
	public String getDesiredCoinSide() {
		return this.payload.getDesiredCoin();
	}

	public BigInteger getBPubExponent() {
		return this.payload.getBPubExponent(); 
	}

	public BigInteger getBPrivExponent() {
		return this.payload.getBPrivExponent();
	}

	public void addSignature(String signature) {
		this.payload.addSignature(signature); 
	}

	public BigInteger[] getKeyA() {
		return this.payload.getKeyA(); 
	}

	public String getOAEPAlg() {
		return this.keyNegotiation.getOAEPAlg();
	}

}
