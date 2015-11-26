package fhwedel.itsproject15.game;

import com.google.gson.Gson;

import fhwedel.itsproject15.game.json.AvailableVersion;
import fhwedel.itsproject15.game.json.CoinFlippingMessage;
import fhwedel.itsproject15.game.json.GameMessage;
import fhwedel.itsproject15.game.json.ProtocolNegotiation;
import fhwedel.itsproject15.network.client.Client;
import fhwedel.itsproject15.network.client.ClientUsingTLS;
import fhwedel.itsproject15.network.server.Server;
import fhwedel.itsproject15.network.server.ServerUsingTLS;

/**
 * Interface for a game that uses SRA and communicates over a network using TLS.
 * 
 * @author tevua
 * @version 1.0
 */
public interface SRAGame {

	/**
	 * Gets called by the server when a new player joins the game.
	 * 
	 * @param id
	 *            identifier for the player
	 */
	public void newPlayer(int id);

	public void receivedMessage(String message); 
}
