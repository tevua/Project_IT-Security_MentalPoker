package de.fhwedel.itsproject1516.game;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Security;
import java.util.Map;
import java.util.Scanner;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import gr.planetz.PingingService;
import gr.planetz.impl.HttpPingingService;

public class CoinFlippingConsole {

	//private static final String DEFAULT_HOST = "fluffels.de";
	//private static final int DEFAULT_PORT = 50000;
	// private static final String DEFAULT_HOST = "87.106.18.90";
	// private static final int DEFAULT_PORT = 6882;
	private static final String DEFAULT_HOST = "54.77.97.90";
	private static final int DEFAULT_PORT = 4444;
	private static final String DEFAULT_TRUST = "root";
	private static final String DEFAULT_PASSWORD_TRUST = "fhwedel";
	private static final String DEFAULT_KEY = "client";
	private static final String DEFAULT_PASSWORD_KEY = "fhwedel";
	private PrintStream out;
	private PrintStream outOptional;
	private InputStream in;
	private String name = "Player 1";
	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;
	private Integer[] sids = new Integer[] { 0, 1, 2, 11 };
	private String trust = DEFAULT_TRUST;
	private String passwordTrust = DEFAULT_PASSWORD_TRUST;
	private String key = DEFAULT_KEY;
	private String passwordKey = DEFAULT_PASSWORD_KEY;
	private boolean displayReceivedSentMessage = true;
	private boolean isServer;
	private boolean continuousGames = false;
	private boolean clientUseBroker = false;
	private CoinFlipping server = null;

	public static void main(String[] args) {
		new CoinFlippingConsole().init(System.out, System.in);
	}

	public void init(PrintStream out, InputStream in) {
		this.out = out;
		if (this.displayReceivedSentMessage)
			this.outOptional = out;
		else
			this.outOptional = new PrintStream(new ByteArrayOutputStream());
		this.in = in;
		displayStartMessage();
	}

	private void displayStartMessage() {
		out.println(" _________________________________________________________________________ ");
		out.println("|        ____      _         _____ _ _             _                      |");
		out.println("|       / ___|___ (_)_ __   |  ___| (_)_ __  _ __ (_)_ __   __ _          |");
		out.println("|      | |   / _ \\| | '_ \\  | |_  | | | '_ \\| '_ \\| | '_ \\ / _` |         |");
		out.println("|      | |__| (_) | | | | | |  _| | | | |_) | |_) | | | | | (_| |         |");
		out.println("|       \\____\\___/|_|_| |_| |_|   |_|_| .__/| .__/|_|_| |_|\\__, |         |");
		out.println("|                                     |_|   |_|            |___/          |");
		out.println("|_________________________________________________________________________|");

		out.println("\nWelcome to CoinFlipping. Would you like to act as server? (y/n)");
		waitForPlayAs();
	}

	protected void waitForPlayAs() {
		out.print("> ");
		// Wait for input
		Scanner sc = new Scanner(in);
		if (sc.hasNextLine()) {
			String input = sc.nextLine();
			switch (input) {
			case "y":
			case "Y":
				this.isServer = true;
				out.println("Acting as server. What would you like to do?");
				waitForInputServer();
				break;
			default:
				this.isServer = false;
				out.println("Acting as client. What would you like to do?");
				waitForInputClient();
			}
		}
	}

	protected PrintStream getOut() {
		return this.out;
	}

	protected PrintStream getOutOptional() {
		return this.outOptional;
	}

	protected void displayWinningMessage() {
		out.println(" __   __           __        __          _ ");
		out.println(" \\ \\ / /__  _   _  \\ \\      / /__  _ __ | |");
		out.println("  \\ V / _ \\| | | |  \\ \\ /\\ / / _ \\| '_ \\| |");
		out.println("   | | (_) | |_| |   \\ V  V / (_) | | | |_|");
		out.println("   |_|\\___/ \\__,_|    \\_/\\_/ \\___/|_| |_(_)");
		out.println("                                           ");
		out.println("Congratulations, " + this.name + "!");

	}

	public void displayLosingMessage() {
		out.println(" __   __            _              _   _ ");
		out.println(" \\ \\ / /__  _   _  | |    ___  ___| |_| |");
		out.println("  \\ V / _ \\| | | | | |   / _ \\/ __| __| |");
		out.println("   | | (_) | |_| | | |__| (_) \\__ \\ |_|_|");
		out.println("   |_|\\___/ \\__,_| |_____\\___/|___/\\__(_)");
		out.println("                                         ");
		out.println("Sorry, " + this.name + "!");

	}

	protected void waitForInput() {
		if (this.isServer)
			waitForInputServer();
		else
			waitForInputClient();
	}

	// ###################### Server #######################################

	protected void waitForInputServer() {
		out.print("> ");
		Scanner sc = new Scanner(in);
		if (sc.hasNextLine()) {
			String input = sc.nextLine();
			processInputServer(input);
		}
	}

	private void processInputServer(String input) {
		switch (input) {
		case "s":
		case "start":
			out.println("Waiting for clients. This might take a while!");
			CoinFlipping server = new CoinFlipping(name, port, sids, trust, passwordTrust, key, passwordKey, this);
			// client.startGame(new String[] { "tail", "blah" });
			break;
		case "c":
		case "configure": // settings;
			displaySettingsServer();
			break;
		case "q":
		case "quit":
			System.exit(0);
			break;
		case "help":
			displayHelpServer();
			break;
		default:
			out.println("Sorry, we could not recognize the command " + input);
			displayHelpServer();
		}
	}

	private void displayHelpServer() {
		out.println("You can use the following commands to play Coin Flipping: ");
		out.println("   start      : Starts the server and waits for clients to connect. ");
		out.println("   configure  : Displays the settings and allows you to configure them.");
		out.println("   help       : Displays this help.");
		out.println("   quit       : Exits the game.");
		waitForInputServer();
	}

	private void displaySettingsServer() {
		out.println("The current settings of Coin Flipping are: ");
		out.println("   Name : " + this.name);
		out.println("   Port : " + this.port);
		out.println("   Display all messages          : " + this.displayReceivedSentMessage);
		out.println("   Wait continuously for clients : " + this.continuousGames);
		out.println("\nWhich one would you like to configure? (name, port, display)");
		waitForSettingsInputServer();
	}

	private void waitForSettingsInputServer() {
		out.print("settings > ");
		// Wait for input
		Scanner sc = new Scanner(in);
		if (sc.hasNextLine()) {
			String input = sc.nextLine();
			processSettingsInputServer(input);
		}
	}

	private void processSettingsInputServer(String input) {
		Scanner sc = new Scanner(in);
		switch (input) {
		case "n":
		case "name":
			out.print("New name : ");
			if (sc.hasNextLine()) {
				String newName = sc.nextLine();
				this.name = newName;
			}
			break;
		case "p":
		case "port":
			out.print("New port : ");
			if (sc.hasNextLine()) {
				Integer newPort = Integer.parseInt(sc.nextLine());
				this.port = newPort;
			}
			break;
		case "d":
		case "display":
			this.displayReceivedSentMessage = !this.displayReceivedSentMessage;
			if (this.displayReceivedSentMessage) {
				this.out.println("Now displaying all messages.");
				this.outOptional = this.out;
			} else {
				this.out.println("Not displaying any messages.");
				this.outOptional = new PrintStream(new ByteArrayOutputStream());
			}
			break;
		case "none":
			break;
		default:
			out.println("Sorry, we could not recognize " + input);
		}
		waitForInputClient();
	}

	// ###################### Client #######################################

	protected void waitForInputClient() {
		out.print("> ");
		Scanner sc = new Scanner(in);
		if (sc.hasNextLine()) {
			String input = sc.nextLine();
			processInputClient(input);
		}
	}

	private void processInputClient(String input) {
		switch (input) {
		case "p":
		case "play":
			String host = null;
			Integer port = null;
			if (this.clientUseBroker) {
				String someServer = getRandomServer();
				host = someServer.split(":")[0];
				port = Integer.parseInt(someServer.split(":")[1]);
			} else {
				host = this.host;
				port = this.port;
			}
			if (host != null && port != null) {
				out.println("Trying to connect to " + this.host + ":" + this.port + " to start a new game.");
				out.println("Please be patient while we are waiting for the result.");
				CoinFlipping client = new CoinFlipping(name, host, port, sids, trust, passwordTrust, key, passwordKey,
						this);
				client.startGame(new String[] { "tail", "blah" });
			}
			break;
		case "c":
		case "configure": // settings;
			displaySettingsClient();
			break;
		case "q":
		case "quit":
			System.exit(0);
			break;
		case "help":
			displayHelpClient();
			break; // display help break;
		default:
			out.println("Sorry, we could not recognize the command " + input);
			displayHelpClient();
		}

	}

	private void displayHelpClient() {
		out.println("You can use the following commands to play Coin Flipping: ");
		out.println("   play      : Starts a game of coin flipping. ");
		out.println("   configure : Displays the settings and allows you to configure them.");
		out.println("   help      : Displays this help.");
		out.println("   quit      : Exits the game.");
		waitForInputClient();
	}

	private void displaySettingsClient() {
		out.println("The current settings of Coin Flipping are: ");
		out.println("   Name : " + this.name);
		out.println("   Host : " + this.host);
		out.println("   Port : " + this.port);
		out.println("   Display all messages     : " + this.displayReceivedSentMessage);
		out.println("   Use Broker to find server: " + this.clientUseBroker);
		out.println("\nWhich one would you like to configure? (name, host, port, display, broker)");
		waitForSettingsInputClient();
	}

	private void waitForSettingsInputClient() {
		out.print("settings > ");
		// Wait for input
		Scanner sc = new Scanner(in);
		if (sc.hasNextLine()) {
			String input = sc.nextLine();
			processSettingsInputClient(input);
		}
	}

	private void processSettingsInputClient(String input) {
		Scanner sc = new Scanner(in);
		switch (input) {
		case "n":
		case "name":
			out.print("New name : ");
			if (sc.hasNextLine()) {
				String newName = sc.nextLine();
				this.name = newName;
			}
			break;
		case "host":
			out.print("New host : ");
			if (sc.hasNextLine()) {
				String newHost = sc.nextLine();
				this.host = newHost;
			}
			break;
		case "p":
		case "port":
			out.print("New port : ");
			if (sc.hasNextLine()) {
				Integer newPort = Integer.parseInt(sc.nextLine());
				this.port = newPort;
			}
			break;
		case "d":
		case "display":
			this.displayReceivedSentMessage = !this.displayReceivedSentMessage;
			if (this.displayReceivedSentMessage) {
				this.out.println("Now displaying all messages.");
				this.outOptional = this.out;
			} else {
				this.out.println("Not displaying any messages.");
				this.outOptional = new PrintStream(new ByteArrayOutputStream());
			}
			break;
		case "b":
		case "broker":
			this.clientUseBroker = !this.clientUseBroker;
			if (this.clientUseBroker) {
				this.out.println("Now using broker to find server to play with.");
			} else {
				this.out.println("Now using given host and port to find server to play with.");
			}
			break;
		case "none":
			break;
		default:
			out.println("Sorry, we could not recognize " + input);
		}
		waitForInputClient();
	}

	public void couldNotConnectToServer() {
		out.println("Could not connect to server. Message was not sent.");
		waitForInputClient();
	}

	private String getRandomServer() {

		try {
			final PingingService service2 = new HttpPingingService("https://52.35.76.130:8443/broker/1.0/players", "",
					"", "root", "fhwedel");
			Map<String, String> map = service2.getPlayersDirectlyOverHttpGetRequest();
			Map.Entry<String, String> entry = map.entrySet().iterator().next();
			String key = entry.getKey();
			String value = entry.getValue();
			return value;

		} catch (Exception e) {
			out.println(e);
		}
		return "";
	}
}
