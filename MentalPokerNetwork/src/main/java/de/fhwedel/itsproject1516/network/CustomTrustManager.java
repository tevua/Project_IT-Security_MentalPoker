package de.fhwedel.itsproject1516.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class CustomTrustManager implements X509TrustManager {

	/** static variable to say if self signed certificates should be accepted */
	public static final int ALWAYS = 0;
	/**
	 * if self-signed certificate are accepted with permit only, the user is
	 * asked on the standard output stream
	 */
	public static final int WITH_PERMIT = 1;
	public static final int NEVER = 2;

	private int acceptSelfSigned;

	private X509TrustManager origManager;

	private boolean saveSelfSignCert;
	private String passwordKeyStore;
	private String filenameKeyStore;

	public CustomTrustManager(X509TrustManager origManager, int acceptSelfSigned, boolean saveSelfSignCert,
			String filenameKeyStore, String passwordKeyStore) {
		this.origManager = origManager;
		this.acceptSelfSigned = acceptSelfSigned;
		this.saveSelfSignCert = saveSelfSignCert;
		this.passwordKeyStore = passwordKeyStore;
		this.filenameKeyStore = filenameKeyStore;
	}

	private void checkTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			// This will call the original trust manager
			// which will throw an exception if it doesn't know the certificate
			origManager.checkServerTrusted(chain, authType);
		} catch (CertificateException e) {
			// an exception is thrown if the original trust manager doesn't
			// accept the certificate (which is in chain[0])
			if (this.acceptSelfSigned == ALWAYS || this.acceptSelfSigned == WITH_PERMIT) {
				boolean selfSigned = false;
				// Try to verify certificate signature with its own public key
				try {
					PublicKey key = chain[0].getPublicKey();
					chain[0].verify(key);
					selfSigned = true;
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException
						| SignatureException e1) {
					selfSigned = false;
				}
				// get the common name
				String cn = null;
				if (selfSigned) {
					cn = X509CertGenerator.getCN(chain[0]);
				}
				if (selfSigned && this.acceptSelfSigned == WITH_PERMIT) {
					System.out.println("Certificate from " + cn + " is self-signed. Accept? [j/n]");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					try {
						String s = br.readLine();
						if (!(s.equals("j") || s.equals("J"))) {
							selfSigned = false;
						}
					} catch (IOException e1) {
						// ignore
					}
				}

				if (selfSigned && this.saveSelfSignCert) {
					// save the self signed certificate (if it was accepted)
					try {
						X509CertGenerator.storePublic(chain[0], cn, this.passwordKeyStore, this.filenameKeyStore);
					} catch (KeyStoreException | NoSuchAlgorithmException | IOException e1) {
						// just print the error
						System.err.println(
								"An error was encountered while trying to save the certificate. Certificate not saved. "
										+ e1);
					}
				}
				if (this.acceptSelfSigned == NEVER || !selfSigned) {
					throw new CertificateException("This is not a self-signed certificate");
				}
			}
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("checkclient");
		checkTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("checkserver");
		checkTrusted(chain, authType);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return this.origManager.getAcceptedIssuers();
	}

}
