import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.SRAPublicParameters;
import org.bouncycastle.util.encoders.Hex;

/**
 * The user of the coin flipping. He needs a brain.
 * 
 * @author tevua
 * @version 1.0
 */
public class CoinFlippingUser {

	// name of the user
	private String name;
	// if the user is the initiator (the person who starts the game and is
	// responsible for coin generation)
	private boolean initiator;
	// the brain of the user
	private CoinFlipping brain;

	//this is just for test purposes to switch the printed stuff out or not
	private boolean debug = false; 
	/**
	 * Just sets stuff.
	 * 
	 * @param name
	 *            name of the user.
	 */
	public CoinFlippingUser(String name) {
		this.name = name;
		this.initiator = false;
		this.brain = new CoinFlipping();
	}

	/**
	 * Decides whether to accept a given p and q or not.
	 * 
	 * @param p
	 *            p and q
	 * @return true when the user agrees with p and q
	 */
	public boolean decidePQ(SRAPublicParameters pq) {
		boolean agreed = this.brain.controlPQ(pq);
		if (agreed) {
			if (this.debug) System.out.println("  " + this.name + " accepts p and q.");
		} else {
			if (this.debug) System.out.println("  " + this.name + " declines p and q.");
		}
		return agreed;
	}

	/**
	 * Creating a suggestion for p and q.
	 * 
	 * @return suggestion
	 */
	public SRAPublicParameters getSuggestion() {
		int strength = 768;
		int certainty = 25;
		return brain.suggest(strength, certainty);
	}

	/**
	 * Function that is called at a start of a game for player 1, which is the
	 * player that suggests the keys and creates the coin.
	 * 
	 * @param b
	 *            the other player
	 * @return the name of the winner
	 */
	public String flipCoin(CoinFlippingUser b) {
		this.initiator = true;

		if (this.debug) System.out.println("  " + this.name + " suggests p and q.");
		// player 1 suggests a number
		boolean agreed = false;
		SRAPublicParameters pq = null;
		//TODO .. nudelt im zweifel für immer
		while (!agreed) {
			pq = getSuggestion();
			if (b.decidePQ(pq)) {
				// player 2 agrees
				agreed = true;
			} else {
				// player 2 has declined the suggestion
				// player 1 and 2 switch roles and the game starts again
				pq = b.getSuggestion();
				if (decidePQ(pq)) {
					// this player agreed on p and q
					agreed = true;
				}
			}
		}

		if (this.debug) System.out.println("INIT SUCCESFULL");

		// 1 and 2 have to create their private keys
		b.prepareForBattle(pq);
		prepareForBattle(pq);

		// player 2 can chose a side
		String betOn = this.brain.betOnSide(b.betOnSide(null));

		if (this.debug) System.out.println("THROWING THE COIN");
		brain.setChosenSide(b.chooseCoinSide(brain.getCoin()));

		if (this.debug) System.out.println("  " + this.name
		 + " decrypts the coin side and sends it back.");

		String result = b.revealChosenSide(brain.getChosenSide());
		this.brain.setResult(result);

		if (this.debug) System.out.println("RESULT: " + result);

		if (b.checkResults(reveal()) && checkResults(b.reveal())) {
			if (betOn.compareTo(result) == 0) {
				return this.name;
			} else {
				return b.getName();
			}
		} else {
			return "none - someone cheated";
		}

	}

	/**
	 * Returns the name of this user.
	 * 
	 * @return the name
	 */
	private String getName() {
		return this.name;
	}

	private String betOnSide(String otherBet) {
		return this.brain.betOnSide(otherBet);
	}

	/**
	 * Checks the result
	 * 
	 * @param key
	 *            the private key of the other person
	 * @return true if everything went well
	 */
	public boolean checkResults(AsymmetricCipherKeyPair key) {
		if (this.debug) System.out.println("  " + this.name + " checks the results.");
		return this.brain.checkResult(this.initiator, key);
	}

	/**
	 * Revealing the private key. Should only be called when the game is over.
	 * 
	 * @return the private key
	 */
	public AsymmetricCipherKeyPair reveal() {
		return this.brain.getKey();
	}

	/**
	 * Preparing for Battle (aka generating the private keys and in the case of
	 * the initiator creating the coin).
	 * 
	 * @param pq
	 *            the agreed on p and q
	 */
	public void prepareForBattle(SRAPublicParameters pq) {
		this.brain.generatePrivateKey(128, 25, pq);
		if (this.debug) System.out.println("  " + this.name + " has created the private keys.");
		if (this.initiator) {
			brain.createCoin();
			if (this.debug) System.out.println("  " + this.name + " created the coin.");
		}
	}

	/**
	 * Revealing the chosen side. Only called for the person that is not the
	 * initiator.
	 * 
	 * @param coinSide
	 *            the still once encrypted coinSide
	 * @return the result
	 */
	public String revealChosenSide(String coinSide) {
		if (this.debug)System.out.println("  " + this.name
				+ " decrypts the coin side and reveals the result.");
		String result = new String(Hex.decode(brain.decryptCoinSide(coinSide)));
		this.brain.setResult(result);
		return result;
	}

	/**
	 * Choosing a coin side. Only called for the person that is not the
	 * initiator.
	 * 
	 * @param coin
	 *            the once encrypted coin to choose a side from
	 * @return the now twice encrypted chosen side
	 */
	public String chooseCoinSide(String[] coin) {
		if (this.debug)System.out.println("  " + this.name
				+ " chooses a coin side and encrypts it.");
		return brain.chooseCoinSide(coin);
	}
}
