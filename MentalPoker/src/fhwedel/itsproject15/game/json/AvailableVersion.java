package fhwedel.itsproject15.game.json;

import java.util.Arrays;

/**
 * Part of the protocol negotiation of the protocol for Coin Flipping : the
 * available versions.
 * 
 * @author gypsy
 *
 */
public class AvailableVersion {

	/** the available versions */
	private String[] versions;

	/**
	 * Constructor

	 * @param v
	 *            array of the available versions
	 */
	public AvailableVersion(String[] v) {
		this.versions = v;
	}

	/**
	 * Return the available versions as an array.
	 * 
	 * @return the versions
	 */
	public String[] getVersions() {
		return versions;
	}

	/**
	 * Checks the instance if it is equal to a previous version.
	 * 
	 * @param prev
	 *            the previous instance of available version
	 * @return true if both instances are equal
	 */
	public boolean checkPrev(AvailableVersion prev) {
		return ((Arrays.equals(versions, prev.getVersions())));
	}

}
