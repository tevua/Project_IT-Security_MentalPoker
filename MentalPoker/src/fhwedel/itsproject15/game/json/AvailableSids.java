package fhwedel.itsproject15.game.json;

import java.util.Arrays;

public class AvailableSids {
	private Integer[] sids;
	
	public AvailableSids(Integer[] s) {
		this.sids = s; 
	}

	public Integer[] getSids() {
		return this.sids; 
	}
	
	public boolean checkPrev(AvailableSids prev) {
		return ((Arrays.equals(sids, prev.getSids())));
	}
}
