package de.fhwedel.itsproject1516.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

/**
 * Own implementation of the trust manager. Tries to verify a certificate with a
 * given TrustManager (the original Manager which contains a root/multiple root
 * certificates) and if that does not work, the certificate is accepted if
 * self-signed (if the user chose to do so - he can set a flag).
 * 
 * @author tevua
 * @version 1.0
 */
public class OwnTrustManager implements X509TrustManager {

	/** static variable to say if self signed certificates should be accepted */
	public static final int ALWAYS = 0;
	/**
	 * if self-signed certificate are accepted with permit only, the user is
	 * asked on the standard output stream (input comes on a given input
	 * stream...)
	 */
	public static final int WITH_PERMIT = 1;
	/**
	 * when the original trust manager does not accept the certificate, the
	 * certificate is not accepted
	 */
	public static final int NEVER = 2;

	/** the original trust manager */
	private X509TrustManager originalManager;
	/**
	 * if the certificate is accepted if it is self signed (can be ALWAYS,
	 * WITH_PERMIT or NEVER)
	 */
	private int acceptSelfSigned;
	/**
	 * if the (accepted) certificate is supposed to be saved (only works, if the
	 * original manager does not accept the certificate)
	 */
	private boolean saveSelfSignedCert;
	/**
	 * the input stream from which the answer is read when acceptSelfSigned =
	 * WITH_PERMIT (for the other two values, this can be null)
	 */
	private InputStream in;

	/**
	 * Constructor for the original manager.
	 * 
	 * @param origManager
	 *            the original trust manager
	 * @param acceptSelfSigned
	 *            if a self-signed certificate is supposed to be accepted
	 * @param inputStream
	 *            the input stream (if acceptSelfSigned = ALWAYS | NEVER this
	 *            should be null)
	 * @param save
	 *            if the accepted, self signed certificate is supposed to be
	 *            saved
	 */
	public OwnTrustManager(X509TrustManager origManager, int acceptSelfSigned, InputStream inputStream, boolean save) {
		this.originalManager = origManager;
		this.acceptSelfSigned = acceptSelfSigned;
		this.saveSelfSignedCert = save;
		this.in = inputStream;
	}

	/**
	 * Tests if a given certificate is self signed.
	 * 
	 * @param cert
	 *            the certificate
	 * @return true if the certificate is self signed
	 * @throws CertificateException
	 */
	private boolean isSelfSigned(X509Certificate cert) throws CertificateException {
		try {
			PublicKey key = cert.getPublicKey();
			cert.verify(key);
			return true;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e1) {
			return false;
		}
	}

	/**
	 * Returns the common name for a given certificate.
	 * 
	 * @param cert
	 *            the certificate
	 * @return the common name of the certificate
	 */
	private String getCommonName(X509Certificate cert) {
		try {
			X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
			org.bouncycastle.asn1.x500.RDN cn = x500name.getRDNs(BCStyle.CN)[0];
			return IETFUtils.valueToString(cn.getFirst().getValue());
		} catch (CertificateEncodingException e1) {
			return null;
		}

	}

	/**
	 * Checks if a given certificate chain is to be trusted.
	 * 
	 * @param chain
	 *            the certificate chain
	 * @param authType
	 *            authentication type
	 * @throws CertificateException
	 *             if the certificate is not accepted
	 */
	private void checkTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			// This will call the original trust manager
			// which will throw an exception if it doesn't know the certificate
			originalManager.checkServerTrusted(chain, authType);
		} catch (CertificateException e) {
			// try to verify with its own key (certificate is self signed)
			if (this.acceptSelfSigned == ALWAYS || this.acceptSelfSigned == WITH_PERMIT) {
				boolean selfSigned = isSelfSigned(chain[0]);
				if (!selfSigned) {
					throw new CertificateException("Certificate not self-signed.");
				}
				// ask for permit to save the file
				String commonName = getCommonName(chain[0]);
				if (selfSigned && this.acceptSelfSigned == WITH_PERMIT) {
					System.out.println("Certificate from " + commonName + " is self-signed. Accept? [j/n]");
					BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
					try {
						String s = br.readLine();
						if (!(s.equals("j") || s.equals("J"))) {
							selfSigned = false;
						}
					} catch (IOException e1) {
						// ignore
					}
				}
				// if you are supposed to save the file, save it
				if (selfSigned && this.saveSelfSignedCert) {
					// TODO save the file
				}
			} else {
				throw new CertificateException("Not accepting this certificate with given certificate chain.");
			}
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		checkTrusted(chain, authType);

	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		checkTrusted(chain, authType);

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

}
