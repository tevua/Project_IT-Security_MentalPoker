package fhwedel.itsproject15.game.json;

/**
 * Part of the protocol for Coin Flipping : the protocol negotiation. Only works
 * for two players.
 * 
 * @author tevua
 */
public class ProtocolNegotiation {

	/** the chosen version */
	private String version;

	/** the available versions */
	private AvailableVersion[] availableVersions;

	/**
	 * Constructor for the protocol negotiation
	 * 
	 * @param avs
	 *            the available version for the first person of the protocol
	 */
	public ProtocolNegotiation(AvailableVersion[] avs) {
		this.availableVersions = avs;
	}

	/**
	 * Return the array of the available versions (array should only ever
	 * contain two elements).
	 * 
	 * @return the array of available versions
	 */
	private AvailableVersion[] getAvailableVersions() {
		return this.availableVersions;
	}

	/**
	 * Returns the chosen version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return this.version;
	}

	/**
	 * Adds the chosen common version to the protocol negotiation.
	 * 
	 * @param chosenVersion
	 *            the chosen version
	 */
	public void addCommonVersion(String chosenVersion) {
		this.version = chosenVersion;
	}

	/**
	 * Adds version to the available versions.
	 * 
	 * @param versions
	 *            the array of available versions
	 * @param name
	 *            the name of the person
	 */
	public void addVersions(String[] versions) {
		int length = this.availableVersions.length;
		AvailableVersion[] newAvailableVersions = new AvailableVersion[length + 1];
		// copy old array to new array
		for (int i = 0; i < length; ++i) {
			newAvailableVersions[i] = this.availableVersions[i];
		}
		newAvailableVersions[length] = new AvailableVersion(versions);
		this.availableVersions = newAvailableVersions;
	}

	/**
	 * Checks if the protocol negotiation equals a previous version of the
	 * protocol negotiation.
	 * 
	 * @param prev
	 *            the previous version
	 * @return true if current equals previous
	 */
	public boolean checkPrev(ProtocolNegotiation prev) {
		boolean result = true;
		// elements in the array should be the same as they were in the previous
		// version (and the order should not change)
		int lengthPrev = prev.getAvailableVersions().length;
		for (int i = 0; i < lengthPrev; ++i) {
			AvailableVersion av1 = this.availableVersions[i];
			result = result && (av1.checkPrev(prev.getAvailableVersions()[i]));
		}
		if (prev.getVersion() != null) {
			result = result && prev.getVersion().equals(this.getVersion());
		}
		return result;
	}

	/**
	 * Checks if the protocol is valid.
	 * 
	 * @param pid
	 *            the protocol identifier
	 * @return true if the protocol negotiation is valid
	 */
	public boolean checkValid(Integer pid) {
		switch (pid) {
		case 0:
			// there has to be one list with avaible versions
			return (this.availableVersions != null && this.availableVersions[0] != null);

		default:
			// chosen version has to be part of both available version arrays
			return this.availableVersions != null && this.availableVersions.length == 2
					&& this.availableVersions[0] != null && this.availableVersions[1] != null && this.version != null
					&& arrayContainsVersion(this.availableVersions[0].getVersions())
					&& arrayContainsVersion(this.availableVersions[1].getVersions());

		}

	}

	/**
	 * Checks if a given array contains the chosen version number.
	 * 
	 * @param a
	 *            the array
	 * @return true if the array contains this.version
	 */
	private boolean arrayContainsVersion(String[] a) {
		boolean found = false;
		for (final String i : a) {
			if (i.equals(this.version)) {
				found = true;
			}
		}
		return found;
	}

	/**
	 * Calculates the common version if there are two players (two elements in
	 * the array availableVersions).
	 * 
	 * @return the common version of the two players
	 */
	public String calculateCommonVersion() {
		if (this.availableVersions.length == 2) {
			String[] v1 = this.availableVersions[0].getVersions();
			String[] v2 = this.availableVersions[1].getVersions();
			String common = null;
			if (v1 != null && v2 != null) {
				for (int i = 0; i < v1.length; i++) {
					for (int j = 0; j < v2.length; j++) {
						if (v1[i].equals(v2[j])) {
							common = v1[i];
						}
					}
				}
			}
			return common;
		} else {
			return null;
		}
	}
}
