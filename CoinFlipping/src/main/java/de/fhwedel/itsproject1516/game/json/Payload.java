package de.fhwedel.itsproject1516.game.json;

import java.math.BigInteger;

public class Payload {
	private String[] initialCoin;
	private String desiredCoin;
	private String[] encryptedCoin;
	private String enChosenCoin;
	private String deChosenCoin;
	private BigInteger[] keyA;
	private BigInteger[] keyB;
	private String signatureA;

	public Payload(String[] inCoin, String[] enCoin) {
		this.initialCoin = inCoin;
		this.encryptedCoin = enCoin;
	}

	public void addChosenCoinSide(String desired, String encrypted) {
		this.desiredCoin = desired;
		this.enChosenCoin = encrypted;
	}
	
	public void addKeyA(BigInteger[] key) {
		this.keyA = key; 
	}

	public String[] getInitialCoin() {
		return this.initialCoin;
	}

	public String[] getEncryptedCoin() {
		return this.encryptedCoin;
	}

	public String getDesiredCoin() {
		return this.desiredCoin;
	}

	public String getEnChosenCoin() {
		return this.enChosenCoin;
	}

	public String getDeChosenCoin() {
		return this.deChosenCoin;
	}

	public boolean checkPrev(Payload prev) {
		// check initialCoin
		boolean result = this.initialCoin.length == 2 && prev.getInitialCoin().length == 2
				&& this.initialCoin[0].equals(prev.getInitialCoin()[0])
				&& this.initialCoin[1].equals(prev.getInitialCoin()[1]);

		// check encryptedCoin
		result = result && this.encryptedCoin.length == 2 && prev.getEncryptedCoin().length == 2
				&& this.encryptedCoin[0].equals(prev.getEncryptedCoin()[0])
				&& this.encryptedCoin[1].equals(prev.getEncryptedCoin()[1]);

		// check other stuff
		if (prev.getDesiredCoin() != null) {
			result = result && this.desiredCoin.equals(prev.getDesiredCoin())
					&& (this.desiredCoin.equals(this.initialCoin[0]) || this.desiredCoin.equals(this.initialCoin[1]));

		}
		if (prev.getEnChosenCoin() != null) {
			result = result && this.enChosenCoin.equals(prev.getEnChosenCoin());
		}
		if (prev.getDeChosenCoin() != null) {
			result = result && this.deChosenCoin.equals(prev.getDeChosenCoin());
		}
		return result;
	}

	public void addDeChosenCoin(String side) {
		this.deChosenCoin = side; 
	}

	public void addKeyB(BigInteger[] key) {
		this.keyB = key; 
	}

	public BigInteger getBPubExponent() {
		return this.keyB[0]; 
	}
	
	public BigInteger getBPrivExponent() {
		return this.keyB[1]; 
	}
	
	public BigInteger getAPubExponent() {
		return this.keyA[0]; 
	}
	
	public BigInteger getAPrivExponent() {
		return this.keyA[1]; 
	}

	public void addSignature(String signature) {
		this.signatureA = signature; 
	}

	public BigInteger[] getKeyA() {
		return this.keyA; 
	}
}
