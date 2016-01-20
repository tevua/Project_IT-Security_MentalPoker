package de.fhwedel.itsproject1516.network;

public interface TLSNetworkGame {

	public void receivedMessage(String message);

	public void stopGame();

	public void gameHasStopped();

	public void couldNotConnect();
	
}
