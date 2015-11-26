package fhwedel.itsproject15.game;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import com.google.gson.Gson;

import fhwedel.itsproject15.game.crypto.GameCrypto;

import fhwedel.itsproject15.game.json.CoinFlippingMessage;

import fhwedel.itsproject15.network.client.Client;
import fhwedel.itsproject15.network.client.ClientUsingTLS;
import fhwedel.itsproject15.network.server.Server;
import fhwedel.itsproject15.network.server.ServerUsingTLS;

/***
 * Coin Flipping game. 
 * 
 * TODO: OAEP missing
 * 
 * TODO: final check of the result (for B and A)
 * @author tevua
 */
public class CoinFlipping implements SRAGame {

	private Server server = null;
	private Client client = null;

	/** the gson used for parsing json */
	private Gson gson;
	
	/**
	 * the current coin flipping message (gets replaced when the new one equals
	 * the old one)
	 */
	private CoinFlippingMessage mPrev;

	/** the available security identifiers of the player */
	private Integer[] availableSids;
	// private Thread waitingForClient;

	/** the initial coin (in plain text) */
	private String[] initialCoin;

	/** KeyPair used for de-/encryption */
	private GameCrypto crypto;

	/**
	 * Initializes the game regardless if player plays as server or client.
	 * 
	 * @param availableSids
	 *            the available Security identifiers
	 */
	private void init(Integer[] availableSids) {
		this.gson = new Gson();
		this.availableSids = availableSids;
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Constructor when this person is the server (player A).
	 * 
	 * @param port
	 *            the port
	 * @param availableSids
	 *            the available security identifiers
	 * @param coin
	 *            the initial coin
	 */
	public CoinFlipping(int port, Integer[] availableSids, String[] coin) {
		this.server = new ServerUsingTLS(this);
		this.server.start(port);
		init(availableSids);
		this.initialCoin = coin;
	}

	/**
	 * Constructor when this person is the client (player B).
	 * 
	 * @param host
	 *            the host address of the server (player A)
	 * @param port
	 *            the port to connect
	 * @param availableSids
	 *            the available security identifiers
	 */
	public CoinFlipping(String host, int port, Integer[] availableSids) {
		this.client = new ClientUsingTLS(this);
		this.client.connect(host, port);
		init(availableSids);
	}

	@Override
	public void newPlayer(int id) {
		// as this is coinFlipping only one player is allowed to join
		if (id == 1) {
			startGame();
		}
	}

	/**
	 * Player A: starts the game
	 * 
	 * Sending message 0.
	 * 
	 * Possible errors: no errors.
	 */
	private void startGame() {
		// 0: A --> B : "availableVersions": List of available protocol versions
		this.mPrev = new CoinFlippingMessage(0, 0, "OK", new String[] { "1.0" });
		sendMessage(mPrev);
	}

	private void sendMessage(CoinFlippingMessage message) {
		String m = gson.toJson(message);
		if (this.server != null) {
			this.server.send(m);
		} else if (this.client != null) {
			this.client.send(m);
		}
		// waitSeconds();
	}

	@Override
	public void receivedMessage(String message) {
		// if (this.waitingForClient != null) {
		// this.waitingForClient.interrupt();
		// }
		CoinFlippingMessage m = gson.fromJson(message, CoinFlippingMessage.class);
		// check if the other encountered an error
		if (m.checkError()) {
			// check if the current message equals the previous
			if (m.checkPrev(this.mPrev)) {

				this.mPrev = m;

				switch (this.mPrev.getProtocolId()) {
				case 0:
					processZero();
					break;
				case 1:
					processOne();
					break;
				case 2:
					processTwo();
					break;
				case 3:
					processThree();
					break;
				case 4:
					processFour();
					break;
				case 5:
					processFive();
					break;
				case 6:
					processSix();
					break;
				case 7:
					processSeven();
					break;
				default:
					errorEncountered(101, "Unknown protokoll id.");
				}
			} else {
				errorEncountered(102, "Current protocol doesn't match previous.");
			}
		} else {
			stopGame();
		}
	}

	/**
	 * Player B: Respond to protocol negotiation.
	 * 
	 * Processing the first message with protocolId = 0 (protocol negotiation).
	 * Returns with the message with protocolId = 1 (still protocol
	 * negotiation).
	 * 
	 * Possible errors: 1, 2
	 */
	private void processZero() {
		// B: checks availableVersions not empty and has proper values
		// => error:No proper value in protocol list
		if (this.mPrev.checkValidProtocolNegotiation()) {
			this.mPrev.addAvailableVersions(new String[] { "1.0" });
			// checks for common protocol
			// => error:No common protocol
			String commonVersion = this.mPrev.computeCommonVersion();
			if (commonVersion == null) {
				errorEncountered(2, "No common protocol.");
			} else {
				// 1: B --> A: "version" : chosen protocol version
				this.mPrev.addCommonVersion(commonVersion);
				this.mPrev.increaseProtocolId();
				sendMessage(mPrev);
			}
		} else {
			errorEncountered(1, "No proper value in protocol list.");
		}

	}

	/**
	 * Player A: Finish protocol negotiation.
	 * 
	 * Processes message with protocolId = 1 (protocol negotiation), returns
	 * with message with protocolId = 2 (key negotiation).
	 * 
	 * Possible errors: 3
	 */
	private void processOne() {
		if (this.mPrev.checkValidProtocolNegotiation()) {
			// 2: A --> B: "availableSIDs" : List of security identifiers
			this.mPrev.addAvailableSids(this.availableSids);
			this.mPrev.increaseProtocolId();
			sendMessage(mPrev);
		} else {
			errorEncountered(3, "Error checking protocol: not proper.");
		}
	}

	/**
	 * Player B: Generate p, q
	 * 
	 * Processes message with protocolId = 2 (key negotiation) and responds with
	 * message with protocolId = 3 (key negotiation).
	 * 
	 * Possible errors: 4, 5, 6
	 */
	private void processTwo() {

		// B: validates availableSIDs
		// => error: No proper value in availableID list
		if (this.mPrev.checkValidKeyNegotiation()) {
			this.mPrev.addAvailableSids(this.availableSids);

			// check for common security id
			// => error: No common security id
			Integer commonSid = this.mPrev.calculateCommonSid();
			if (commonSid == null) {
				errorEncountered(5, "No common security id");
			} else {
				this.mPrev.addCommonSid(commonSid);

				this.crypto = new GameCrypto(this.mPrev.getKeySize());
				this.crypto.generatePQ();
				try {
					this.crypto.generateKeyPair();

					// 3: B --> A: "availableSIDs": Added own settings
					// "sid" : choosen security identifier
					// "p" : B calculates p
					// "q" : B calculates q
					this.mPrev.addPandQ(this.crypto.getP(), this.crypto.getQ());
					this.mPrev.increaseProtocolId();

					sendMessage(mPrev);

				} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
					errorEncountered(6, "Error with cryptography. Sorry. My fault.");
				}

			}
		} else {
			errorEncountered(4, "No proper value in availableId list.");
		}

	}

	/**
	 * Player A: Finishing key negotiation. Encryption of both coin sides
	 * 
	 * Processing the message with protocolId = 3 (key negotiation) and responds
	 * with message with protocolId = 4 (payload).
	 * 
	 * Errors: 6, 7, 8
	 */
	private void processThree() {
		// key negotiation: A: checks SRA-Modulus P and Q.
		// => error: Key negotiation failed.
		if (this.mPrev.checkValidKeyNegotiation()) {
			boolean error = false;
			try {
				this.crypto = new GameCrypto(this.mPrev.getKeySize());
				this.crypto.setPQ(this.mPrev.getP(), this.mPrev.getQ());
				this.crypto.generateKeyPair();
			} catch (Exception e) {
				error = true;
				errorEncountered(8, "Do not agree with p and q. Error creating key pair. ");
			}
			if (!error) {
				// encrypt the two coins
				try {
					// 4: A --> B: "EncryptedCoin" : A encrypts both coins -
					// SRA_a{OAEP{M1}}, SRA_a{OAEP{M2}}, encoded as hex string.
					// "initialCoin" : Names for Head and Tail

					String sEncr1 = new String(Hex.encode(this.crypto.encrypt(this.initialCoin[0].getBytes())));
					String sEncr2 = new String(Hex.encode(this.crypto.encrypt(this.initialCoin[0].getBytes())));

					this.mPrev.addPayload(this.initialCoin, new String[] { sEncr1, sEncr2 });
					this.mPrev.increaseProtocolId();
					sendMessage(this.mPrev);

				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException
						| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
					errorEncountered(6, "Error with cryptography. Sorry. My fault.");
				}
			}
		} else {
			errorEncountered(7, "Key negotiation failed.");
		}

	}

	/**
	 * Player B: Choosing a coin side.
	 * 
	 * Processing the message with protocolId = 4 (payload) and responds with
	 * message with protocolId = 5 (payload).
	 * 
	 * Errors: 6, 9
	 */
	private void processFour() {
		// B: checks key negotiation (A finished in message 3)
		// => error: Error checking key negotiation.
		if (this.mPrev.checkValidKeyNegotiation()) {

			// 5: B --> A
			// "EnChosenCoin" : B choses a random coin M from M1, M2 and
			// encrypts it - SRA_b{SRA_a{OAEP{M}}}, encoded as Hex-String.
			// "desiredCoin" : B choses a coin side

			SecureRandom randomGenerator = new SecureRandom();
			int randomInt = randomGenerator.nextInt(2);
			String desired = this.mPrev.getInitialCoin()[randomInt];
			randomInt = randomGenerator.nextInt(2);
			String chosen = this.mPrev.getEncryptedCoin()[randomInt];

			try {
				String encrypted = new String(Hex.encode(this.crypto.encrypt(Hex.decode(chosen.getBytes()))));
				this.mPrev.addChosenCoinSide(desired, encrypted);
				this.mPrev.increaseProtocolId();
				sendMessage(this.mPrev);

			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException e) {
				errorEncountered(6, "Error with cryptography. Sorry. My fault.");
			}

		} else {
			errorEncountered(9, "Error checking key negotiation.");
		}
	}

	/**
	 * Player A: Decode the chosen coin.
	 * 
	 * Processing message 5 (payload) responds with message 6 (payload).
	 * 
	 * Errors: 6, 10
	 */
	private void processFive() {
		// A: check if payload up to now is proper
		// => error: Error checking payload.
		if (this.mPrev.checkProperPayload()) {
			try {
				// 6: A --> B "deChosenCoin" : A decrypts the enChosenCoin
				// - SRA_b{OAEP{M}}, encoded as HexString
				// "keyA" : A private key (e,d)
				// "signaturA" : A signs something... TODO
				String deCoinSide = new String(
						Hex.encode(this.crypto.decrypt(Hex.decode(this.mPrev.getEnChosenSide().getBytes()))));
				this.mPrev.addDeChosenCoinSide(deCoinSide);
				this.mPrev.addKeyA(this.crypto.getE(), this.crypto.getD());
				this.mPrev.increaseProtocolId();
				sendMessage(this.mPrev);
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
					| NoSuchProviderException | NoSuchPaddingException e) {
				errorEncountered(6, "Error with cryptography. Sorry. My fault.");
			}
		} else {
			errorEncountered(10, "Error checking payload.");
		}
	}

	/**
	 * Player B: Finishing the game.
	 * 
	 * Processing message 6 (payload) responds with message 7 (payload).
	 * 
	 * Errors: 10
	 */
	private void processSix() {
		// B: check if payload is proper
		// => error: Error checking payload.
		if (this.mPrev.checkProperPayload()) {
			try {
				// 7: B --> A: "KeyB" : B returns his private key (e,d)
				String decrypted = new String(this.crypto.decrypt(Hex.decode(this.mPrev.getDeChosenSide().getBytes())));
				this.mPrev.addKeyB(this.crypto.getE(), this.crypto.getD());
				this.mPrev.increaseProtocolId();
				sendMessage(this.mPrev);
				if (decrypted.equals(this.mPrev.getDesiredCoinSide())) {
					System.out.println("Congratulations. You won.");
				} else {
					System.out.println("You lost. Sorry mate.");
				}
				stopGame();
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
					| NoSuchProviderException | NoSuchPaddingException e) {
				errorEncountered(6, "Error with cryptography. Sorry. My fault.");
			}
		} else {
			errorEncountered(10, "Error checking payload.");
		}
	}

	/**
	 * Player A: Finishing the game.
	 * 
	 * Processing message 7, not responding at all.
	 */
	private void processSeven() {
		// payload: A: check everything.
		GameCrypto cryptoB = new GameCrypto(this.mPrev.getKeySize());
		cryptoB.setKeyPair(mPrev.getBPubExponent(), mPrev.getBPrivExponent());
		//figure out who has won
		try {
			String decrypted = new String(cryptoB.decrypt(Hex.decode(this.mPrev.getDeChosenSide().getBytes())));
			if (decrypted.equals(this.mPrev.getDesiredCoinSide())) {
				System.out.println("You lost. Sorry mate.");
			} else {
				System.out.println("Congratulations. You won.");
			}
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchProviderException | NoSuchPaddingException e) {
			//do nothing
		}
		//TODO recheck encryption
		stopGame();
	}
	
	/**
	 * Encountered an error. Sends an error message and then stops the game.
	 * 
	 * @param errorCode
	 *            the error Code (Integer between 0 and 10, or 101 or 102)
	 * @param errorMessage
	 *            the error Message.
	 */
	private void errorEncountered(int errorCode, String errorMessage) {
		CoinFlippingMessage m = new CoinFlippingMessage(0, errorCode, errorMessage, null);
		sendMessage(m);
		stopGame();
	}

	/**
	 * Stops the game.
	 */
	public void stopGame() {
		if (this.server != null) {
			this.server.stop();
			// this.server = null;
		} else if (this.client != null) {
			this.client.disconnect();
			// this.client = null;
		}
	}

	// private void waitSeconds() {
	// final GameKillerThread runnable = new GameKillerThread(this);
	// this.waitingForClient = new Thread(runnable, "waiting");
	// this.waitingForClient.start();
	// }

}
