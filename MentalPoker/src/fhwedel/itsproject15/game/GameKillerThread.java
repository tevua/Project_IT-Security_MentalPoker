package fhwedel.itsproject15.game;

import java.util.concurrent.TimeUnit;

public class GameKillerThread implements Runnable {
	/**
	 * the server
	 */
	SRAGame game;

	/**
	 * Constructor.
	 * 
	 * @param aCp the Coin Flipping
	 **/
	public GameKillerThread(SRAGame aGame) {
		this.game = aGame;
	}

	/**
	 * the run method
	 */
	public void run() {
		boolean interrupted = false;
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			interrupted = true;
		}
		if (!interrupted) {
			//this.game.stopGame();
		}
	}
}
