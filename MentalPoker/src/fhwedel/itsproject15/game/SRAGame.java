package fhwedel.itsproject15.game;

/**
 * Interface for a game that uses SRA and communicates over a network using TLS. 
 * @author tevua
 * @version 1.0
 */
public interface SRAGame {
	
	/**
	 * Gets called by the server or the client when a message is received. 
	 * @param message the message as a String
	 */
	void receivedMessage(String message); 
	
	/**
	 * Gets called by the server when a new player joins the game. 
	 * @param id identifier for the player
	 */
	void newPlayer(int id); 
}
