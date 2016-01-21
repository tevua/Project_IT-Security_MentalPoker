package de.fhwedel.itsproject1516.game;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Security;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import gr.planetz.PingingService;
import gr.planetz.impl.HttpPingingService;

public class CoinFlippingConsole {

	// private static final String DEFAULT_HOST = "fluffels.de";
	// private static final int DEFAULT_PORT = 50000;
	// private static final String DEFAULT_HOST = "87.106.18.90";
	// private static final int DEFAULT_PORT = 6882;
	private static final String DEFAULT_HOST = "54.77.97.90";
	private static final int DEFAULT_PORT = 4444;
	private static final String DEFAULT_TRUST = "root";
	private static final String DEFAULT_PASSWORD_TRUST = "fhwedel";
	private static final String DEFAULT_KEY = "client";
	private static final String DEFAULT_PASSWORD_KEY = "fhwedel";
	private static final Integer[] DEFAULT_SIDS = { 0, 1, 2, 11 };
	private static final String BROKER_URI = "https://52.35.76.130:8443/broker/1.0/messages";
	private PrintStream out;
	private PrintStream outOptional;
	private InputStream in;
	private String name = "Hermione Granger";
	private String host = DEFAULT_HOST;
	private int port = DEFAULT_PORT;
	private LinkedHashSet<Integer> sids = new LinkedHashSet<Integer>(Arrays.asList(DEFAULT_SIDS));
	private String trust = DEFAULT_TRUST;
	private String passwordTrust = DEFAULT_PASSWORD_TRUST;
	private String key = DEFAULT_KEY;
	private String passwordKey = DEFAULT_PASSWORD_KEY;
	private boolean displayReceivedSentMessage = false;
	private boolean isServer;
	private boolean clientUseBroker = false;

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
			CoinFlipping server = new CoinFlipping(name, port, sids.toArray(new Integer[sids.size()]), trust,
					passwordTrust, key, passwordKey, this);
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
		// out.println(" Wait continuously for clients : " +
		// this.continuousGames);
		out.println("   Certificates ");
		out.println("      Root Certificate   : " + trust);
		out.println("         Password        : " + this.passwordTrust);
		out.println("      Server Certificate : " + this.key);
		out.println("         Password        : " + this.passwordKey);
		out.println("   Available Security Identifiers (sids) : ");

		String sidsString = "{";
		for (Integer s : this.sids) {
			sidsString = sidsString.concat(" ".concat(s.toString().concat(",")));
		}
		sidsString = (String) sidsString.subSequence(0, sidsString.length() - 1);
		out.println("      " + sidsString + " }");

		out.println("\nWhich one would you like to configure? (name, port, display, root, "
				+ "\n                                        server, sids)");
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
		case "r":
		case "root":
			out.print("New root certificate: ");
			if (sc.hasNextLine()) {
				String newRoot = sc.nextLine();
				this.trust = newRoot;
			}
			out.print("Password for new root certificate: ");
			if (sc.hasNextLine()) {
				String pw = sc.nextLine();
				this.passwordTrust = pw;
			}
			break;
		case "s":
		case "server":
			out.print("New server certificate: ");
			if (sc.hasNextLine()) {
				String newRoot = sc.nextLine();
				this.key = newRoot;
			}
			out.print("Password for new server certificate: ");
			if (sc.hasNextLine()) {
				String pw = sc.nextLine();
				this.passwordKey = pw;
			}
			break;
		case "sids":
			out.print("Would you like to add a sid? (j/n) ");
			if (sc.hasNextLine()) {
				String yes = sc.nextLine();
				switch (yes) {
				case "j":
				case "J":
					displayAllSids();
					out.print("Which sid would you like to add? ");
					if (sc.hasNextLine()) {
						Integer newSid = Integer.parseInt(sc.nextLine());
						this.sids.add(newSid);
					}
					break;
				default:
					out.print("Which sid would you like to remove? ");
					if (sc.hasNextLine()) {
						Integer newSid = Integer.parseInt(sc.nextLine());
						this.sids.remove(newSid);
					}
				}
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
				out.println("Trying to connect to " + host + ":" + port + " to start a new game.");
				out.println("Please be patient while we are waiting for the result.");
				CoinFlipping client = new CoinFlipping(name, host, port, sids.toArray(new Integer[sids.size()]), trust,
						passwordTrust, key, passwordKey, this);
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
		out.println("   Display all messages      : " + this.displayReceivedSentMessage);
		out.println("   Use Broker to find server : " + this.clientUseBroker);
		out.println("   Certificates ");
		out.println("      Root Certificate   : " + trust);
		out.println("         Password        : " + this.passwordTrust);
		out.println("      Server Certificate : " + this.key);
		out.println("         Password        : " + this.passwordKey);
		out.println("   Available Security Identifiers (sids) : ");

		String sidsString = "{";
		for (Integer s : this.sids) {
			sidsString = sidsString.concat(" ".concat(s.toString().concat(",")));
		}
		sidsString = (String) sidsString.subSequence(0, sidsString.length() - 1);
		out.println("      " + sidsString + " }");

		out.println("\nWhich one would you like to configure? (name, host, port, display, "
				+ "\n                                        broker, root, client, sids)");
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
		case "r":
		case "root":
			out.print("New root certificate: ");
			if (sc.hasNextLine()) {
				String newRoot = sc.nextLine();
				this.trust = newRoot;
			}
			out.print("Password for new root certificate: ");
			if (sc.hasNextLine()) {
				String pw = sc.nextLine();
				this.passwordTrust = pw;
			}
			break;
		case "c":
		case "client":
			out.print("New client certificate: ");
			if (sc.hasNextLine()) {
				String newRoot = sc.nextLine();
				this.key = newRoot;
			}
			out.print("Password for new client certificate: ");
			if (sc.hasNextLine()) {
				String pw = sc.nextLine();
				this.passwordKey = pw;
			}
			break;
		case "sids":
			out.print("Would you like to add a sid? (j/n) ");
			if (sc.hasNextLine()) {
				String yes = sc.nextLine();
				switch (yes) {
				case "j":
				case "J":
					displayAllSids();
					out.print("Which sid would you like to add? ");
					if (sc.hasNextLine()) {
						Integer newSid = Integer.parseInt(sc.nextLine());
						this.sids.add(newSid);
					}
					break;
				default:
					out.print("Which sid would you like to remove? ");
					if (sc.hasNextLine()) {
						Integer newSid = Integer.parseInt(sc.nextLine());
						this.sids.remove(newSid);
					}
				}
			}
			break;
		case "none":
			break;
		default:
			out.println("Sorry, we could not recognize " + input);
		}
		waitForInputClient();
	}

	private void displayAllSids() {
		out.println("Security Identifiers: ");
		out.println("     0 : SRA1024/SHA1\n" + "     1 : SRA2048/SHA1\n" + "     2 : SRA3072/SHA1\n"
				+ "     3 : SRA4096/SHA1\n" + "    10 : SRA1024/SHA256\n" + "    11 : SRA2048/SHA256\n"
				+ "    12 : SRA3072/SHA256\n" + "    13 : SRA4096/SHA256\n" + "    14 : SRA8192/SHA256\n"
				+ "    15 : SRA16384/SHA256\n" + "    20 : SRA2048/SHA512\n" + "    21 : SRA3072/SHA512\n"
				+ "    22 : SRA4096/SHA512\n" + "    23 : SRA8192/SHA512\n" + "    24 : SRA16384/SHA512");

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
			LinkedHashMap<String,String> map2 = new LinkedHashMap<String, String>(map); 
			out.println("Which server would you like to play with? Enter a number. ");
			Iterator it = map2.entrySet().iterator();
			int i = 1; 
		    while (it.hasNext()) {
		        Map.Entry<String, String> pair = (Map.Entry)it.next();
		        this.out.println(i + " : " +  pair.getKey() + " = " + pair.getValue());
		        ++i; 
		    }
		    String value = null; 
		    Scanner sc = new Scanner(in);
		    if (sc.hasNextLine()) {
				Integer newSid = Integer.parseInt(sc.nextLine());
				it = map2.entrySet().iterator();
				i = 0; 
				Map.Entry<String, String> pair = null;
			    while ((i<newSid) && it.hasNext()) {
			    	pair = (Entry<String, String>) it.next();  
			    	++i;
			    }
			    value = pair.getValue(); 
			}
		    if (value == null) {
				Map.Entry<String, String> entry = map.entrySet().iterator().next();
				String key = entry.getKey();
				value = entry.getValue();
		    } 
			return value;

		} catch (Exception e) {
			out.println(e);
		}
		return "";
	}
}
