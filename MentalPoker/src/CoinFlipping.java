import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.SRAPublicParameters;
import org.bouncycastle.util.encoders.Hex;

/**
 * The brain of the coin flipping game. Every player needs their own game
 * (obviously). All the important stuff happens here (e.g creating a suggestion
 * for p and q, creating the coin, choosing a side of the coin).
 * 
 * @author tevua
 * @version 1.0
 */
public class CoinFlipping extends SRAGame {

	// private keys for the encryption and decryption
	private AsymmetricCipherKeyPair keyPair = null;
	// string representing heads of the coin
	private static String coin_heads = "HEADS";
	// string representing tails of the coin
	private static String coin_tails = "TAILS";
	// the coin (if this is _not_ the initiator it is encrypted)
	private String[] coin = new String[2];
	// the chosen coin (encrypted with the other person key)
	private String chosen;
	// the result
	private String result;
	// the side of the coin which is betted upon (is that proper english?
	// probably not)
	private String bet;

	/**
	 * Creates a suggestion for p and q (uses the function createPublic of the
	 * superclass).
	 * 
	 * @param strength
	 *            the strength of p and q
	 * @param certainty
	 *            how certain p and q are primes
	 * @return the public parameters for SRA (p and q)
	 */
	public SRAPublicParameters suggest(int strength, int certainty) {
		return createPublic(strength, certainty);
	}

	/**
	 * Generates the private key (uses the superclass).
	 * 
	 * @param strengthE
	 *            the strength of e
	 * @param certainty
	 *            how certain e is a prime
	 * @param pq
	 *            the public parameters (p and q)
	 */
	public void generatePrivateKey(int strengthE, int certainty,
			SRAPublicParameters pq) {
		this.keyPair = createPrivate(strengthE, certainty, pq);
	}

	/**
	 * Creates the coin (and encrypts it).
	 */
	public void createCoin() {

		String sEncr1 = encrypt(new String(Hex.encode(coin_tails.getBytes())),
				keyPair.getPublic());
		String sEncr2 = encrypt(new String(Hex.encode(coin_heads.getBytes())),
				keyPair.getPublic());

		SecureRandom randomGenerator = new SecureRandom();
		int randomInt = randomGenerator.nextInt(2);

		coin[randomInt] = sEncr1;
		coin[(randomInt + 1) % 2] = sEncr2;
	}

	/**
	 * Returns the encrypted coin.
	 * 
	 * @return the coin
	 */
	public String[] getCoin() {
		return coin;
	}

	/**
	 * Sets the chosen side of the coin. This method is only called for the
	 * initiator.
	 * 
	 * @param chosenCoinSide
	 *            the twice encrypted chosen side of the coin
	 */
	public void setChosenSide(String chosenCoinSide) {
		this.chosen = decryptCoinSide(chosenCoinSide);
	}

	/**
	 * Choosing a side of the coin (randomnly). This method is only called by
	 * the person that is not the initiator.
	 * 
	 * @param coinToChooseFrom
	 *            the encrypted coin to chose from
	 * @return a twice encrypted chosen coin side
	 */
	public String chooseCoinSide(String[] coinToChooseFrom) {
		this.coin = coinToChooseFrom;
		SecureRandom randomGenerator = new SecureRandom();
		int randomInt = randomGenerator.nextInt(2);
		this.chosen = this.coin[randomInt];
		return encrypt(chosen, this.keyPair.getPublic());
	}

	/**
	 * Sets the result (needed later for checking the result).
	 * 
	 * @param result
	 *            the result of the coin throw (HEADS or TAILS).
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Decrypts a coin side.
	 * 
	 * @param coinSide
	 *            the encrypted coin side
	 * @return the decrypted coin side
	 */
	public String decryptCoinSide(String coinSide) {
		this.result = decrypt(coinSide, keyPair.getPrivate());
		return result;
	}

	/**
	 * Returns the chosen side of the coin.
	 * 
	 * @return the chosen side of the coin (which is still encrypted once)
	 */
	public String getChosenSide() {
		return this.chosen;
	}

	/**
	 * Returns the private key of this brain of Coin Flipping. This method
	 * should only be called when the game is over!!
	 * 
	 * @return the key
	 */
	public AsymmetricCipherKeyPair getKey() {
		return this.keyPair;
	}

	/**
	 * # Checking the results of the coin flipping to make sure no one cheated.
	 * 
	 * @param initiator
	 *            is this person was the initiator of this game
	 * @param key
	 *            the key of the other person
	 * @return true if everything is correct
	 */
	public boolean checkResult(boolean initiator, AsymmetricCipherKeyPair key) {
		boolean allGood = true;
		if (!initiator) {
			// decrypt both coin sides and check that they equal coin_heads and
			// coin_tails
			String coinSide1 = this.coin[0];
			String coinSide2 = this.coin[1];
			coinSide1 = decrypt(coinSide1, key.getPrivate());
			coinSide2 = decrypt(coinSide2, key.getPrivate());
			coinSide1 = new String(Hex.decode(coinSide1));
			coinSide2 = new String(Hex.decode(coinSide2));
			if (((coinSide1.compareTo(coin_heads) != 0) && (coinSide1
					.compareTo(coin_tails) != 0))
					|| ((coinSide2.compareTo(coin_heads) != 0) && (coinSide2
							.compareTo(coin_tails) != 0))
					|| (coinSide1.compareTo(coinSide2) == 0)) {
				allGood = false;
			}
		}

		// decrypt the chosen coin side and check that it equals result
		String chosenCoinSide = decrypt(this.chosen, key.getPrivate());
		chosenCoinSide = new String(Hex.decode(chosenCoinSide));
		if (chosenCoinSide.compareTo(this.result) != 0) {
			allGood = false;
		}
		return allGood;
	}

	/**
	 * Betting on a side of the coin to win.
	 * 
	 * @param otherBet
	 *            the bet of the other person (or null if you are free to
	 *            chose).
	 * @return the side you bet on
	 */
	public String betOnSide(String otherBet) {
		if (otherBet == null) {
			SecureRandom randomGenerator = new SecureRandom();
			int randomInt = randomGenerator.nextInt(2);
			if (randomInt == 0) {
				this.bet = coin_heads;
			} else {
				this.bet = coin_tails;
			}
		} else if (otherBet.compareTo(coin_heads) == 0) {
			this.bet = coin_tails;
		} else {
			this.bet = coin_heads;
		}
		return this.bet;
	}

	/**
	 * Gets public parameters and checks them according to stuff. Whatever.
	 * Stuff in this case is if n = pq and if p and q are prime according to a
	 * chosen certainty.
	 * 
	 * @param pq
	 * @return true if it agrees with p and q
	 */
	public boolean controlPQ(SRAPublicParameters pq) {
		BigInteger p = pq.getP();
		BigInteger q = pq.getQ();
		BigInteger n = pq.getModulus();
		int certainty = 30; 

		return ((n.compareTo(p.multiply(q)) == 0) && (p.isProbablePrime(certainty)) && (q
				.isProbablePrime(certainty)));
	}
}
