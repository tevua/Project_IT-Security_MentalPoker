import de.fhwedel.itsproject1516.game.CoinFlipping;
import de.fhwedel.itsproject1516.game.CoinFlippingConsole;

public class test {

	private static final String HOST = "localhost";
	private static final int PORT = 9905;
	//private static final String HOST_KOKO="54.77.97.90"; 
	//private static final int PORT_KOKO=4444; 
	private static final String HOST_KOKO="fluffels.de"; 
	private static final int PORT_KOKO=50000;
	private static final String TRUSTFILE = "root";
	private static final String PASSWORD_TRUST = "fhwedel";
	private static final String KEY_ANNA = "mentalpoker_anna.private";
	private static final String KEY_BOB = "client";
	private static final String PASSWORD_KEY = "fhwedel";

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			System.out.println("sry");
		}
	}

	private static void shortDelay() {
		sleep(500);
	}
	
	private static void longDelay() {
		sleep(5000);
	}
	
	public static void main(String[] args) {
		CoinFlipping client = new CoinFlipping("client", HOST_KOKO, PORT_KOKO, new Integer[]{0,1,2}, TRUSTFILE, PASSWORD_TRUST, KEY_BOB,
				PASSWORD_KEY, new CoinFlippingConsole());
		client.startGame(new String[] { "tail", "blah" }); 
		longDelay();
	}

}
