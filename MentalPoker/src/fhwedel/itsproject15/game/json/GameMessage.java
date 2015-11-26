package fhwedel.itsproject15.game.json;

public class GameMessage {
	
	/** the id of the protocol */
	protected int protocolId;
	/** the status id (0 if no error was encountered) */
	protected int statusId;
	/** the status message ("OK" if no error was encountered) */
	protected String statusMessage;
	/** the protocol negotiation (only modified if protocolId = 0|1) */
	protected ProtocolNegotiation protocolNegotiation;

	public GameMessage(int pid, int s, String m, ProtocolNegotiation pn) {
		this.protocolId = pid;
		this.statusId = s;
		this.statusMessage = m;
		this.protocolNegotiation = pn;
	}

	public int getProtocolId() {
		return this.protocolId;
	}
	
	protected ProtocolNegotiation getProtocolNegotiation() {
		return this.protocolNegotiation; 
	}
	
	public String computeCommonVersion() {
		return this.protocolNegotiation.calculateCommonVersion();
	}

	public void addAvailableVersions(String[] versions) {
		this.protocolNegotiation.addVersions(versions);
	}

	public void addCommonVersion(String commonVersion) {
		this.protocolNegotiation.addCommonVersion(commonVersion);
	}

	public void increaseProtocolId() {
		this.protocolId++;
	}

	public boolean checkError() {
		// check if status and status message fit
		return ((statusId == 0) && (statusMessage.equals("OK")));
	}
	
	public boolean checkValidProtocolNegotiation() {
		return this.protocolNegotiation.checkValid(this.protocolId);
	}
}
