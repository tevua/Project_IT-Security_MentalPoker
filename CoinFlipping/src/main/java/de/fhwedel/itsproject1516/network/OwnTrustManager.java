package de.fhwedel.itsproject1516.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import sun.security.provider.certpath.X509CertPath;

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
	private boolean saveSelfSigned;
	/**
	 * The filename of the keystore where accepted self signed certificate is
	 * supposed to be saved (should be null if saveSelfSigned is false).
	 */
	private String filenameKeyStore;
	/**
	 * The password for the keystore where the accepted self signed certificate
	 * is supposed to be saved (should be null if saveSelfSigned is false).
	 */
	private String passwordKeyStore;
	/**
	 * the input stream from which the answer is read when acceptSelfSigned =
	 * WITH_PERMIT (for the other two values, this can be null)
	 */
	private InputStream in;
	private final Set<TrustAnchor> anchors;

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
	public OwnTrustManager(X509TrustManager origManager, int acceptSelfSigned, InputStream inputStream, boolean save,
			String filename, String password) {
		this.originalManager = origManager;
		this.acceptSelfSigned = acceptSelfSigned;
		this.saveSelfSigned = save;
		this.in = inputStream;
		this.filenameKeyStore = filename;
		this.passwordKeyStore = password;

		X509Certificate[] trusted = origManager.getAcceptedIssuers();
		/** part of the certpathvalidator thing */
		anchors = new HashSet<TrustAnchor>();
		if (trusted != null) {
			for (X509Certificate cert : trusted) {
				anchors.add(new TrustAnchor(cert, null));
			}
		}
		/** */
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
	 * Verifies a sign with a given root certificate.
	 * 
	 * @param root
	 *            the root certificate
	 * @param cert
	 *            the certificate to verify
	 * @return true if the certificate was verified
	 * @throws CertificateException
	 *             we do not accept certificate
	 */
	private boolean verifyCert(X509Certificate root, X509Certificate cert) throws CertificateException {
		try {
			PublicKey key = root.getPublicKey();
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

	private void storeInKeyStore(X509Certificate cert, String alias, String pw, String filename)
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		File keystoreFile = new File(filename);
		char[] password = pw.toCharArray();
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

		if (!keystoreFile.exists()) {
			keystoreFile.createNewFile();
			ks.load(null, null);
		} else {
			// Load the keystore contents
			FileInputStream in = new FileInputStream(keystoreFile);
			ks.load(in, password);
			in.close();
		}

		// Add the certificate
		ks.setCertificateEntry(alias, cert);

		// Save the new keystore contents
		FileOutputStream out = new FileOutputStream(keystoreFile);
		ks.store(out, password);
		out.close();

	}

	private void checkTrustedWithCertPathValidator(X509Certificate[] chain, String authType) throws CertificateException {

		CertPathValidator validator = null;

		try {
			validator = CertPathValidator.getInstance("PKIX");
		} catch (NoSuchAlgorithmException nsae) {
			throw new CertificateException(nsae);
		}

		CertPath path = new X509CertPath(Arrays.asList(chain));

		PKIXParameters params = null;
		try {
			params = new PKIXParameters(anchors);
			// XXX we probably do want to enable revocation, but it's a pain
			// in the ass.
			params.setRevocationEnabled(false);
		} catch (InvalidAlgorithmParameterException iape) {
			throw new CertificateException(iape);
		}

		try {
			validator.validate(path, params);
		} catch (CertPathValidatorException cpve) {
			throw new CertificateException(cpve);
		} catch (InvalidAlgorithmParameterException iape) {
			throw new CertificateException(iape);
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
			if (selfSigned && this.saveSelfSigned) {
				// save the self signed certificate (if it was accepted)
				try {
					storeInKeyStore(chain[0], commonName, this.passwordKeyStore, this.filenameKeyStore);
				} catch (KeyStoreException | NoSuchAlgorithmException | IOException e1) {
					// just print the error
					System.err.println(
							"An error was encountered while trying to save the certificate. Certificate not saved. "
									+ e1);
				}
			}
		} else {
			throw new CertificateException("Not accepting this certificate with given certificate chain.");
		}

	}

	/**
	 * Checks if the original certificate is a certificate authority (and thus
	 * allowed to sign certs). Does not expect that there is a proper
	 * certificate chain.
	 * 
	 * @throws CertificateException
	 *             if the original certificate used to verify the other one is
	 *             not a ca
	 */
	private void checkOriginalIsCA(X509Certificate[] chain) throws CertificateException {
		X509Certificate[] rootCerts = originalManager.getAcceptedIssuers();

		// return for getBasicConstraints():
		// the value of pathLenConstraint if the BasicConstraints extension is
		// present in the certificate and the subject of the certificate is a
		// CA, otherwise -1. If the subject of the certificate is a CA and
		// pathLenConstraint does not appear, Integer.MAX_VALUE is returned to
		// indicate that there is no limit to the allowed length of the
		// certification path.
		for (X509Certificate cert : rootCerts) {
			if (cert.getBasicConstraints() == -1) {
				// check if this certificate was used to verify the original
				// one...
				if (verifyCert(cert, chain[0])) {
					if (cert.getBasicConstraints() == -1) {
						// not a ca .. cert might still be okay, if the one in
						// the chain is
						// the same one used to verify it
						if (!(chain[0].equals(cert))) {
							throw new CertificateException();
						}
					}
				}
			}
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// System.out.println("checkClientTrusted");
		try {
			// This will call the original trust manager
			// which will throw an exception if it doesn't know the certificate
			//originalManager.checkClientTrusted(chain, authType);
			checkTrustedWithCertPathValidator(chain, authType);

			checkOriginalIsCA(chain);

		} catch (CertificateException e) {
			
			checkTrusted(chain, authType);
		}

	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			// This will call the original trust manager
			// which will throw an exception if it doesn't know the certificate
			// originalManager.checkServerTrusted(chain, authType);
			checkTrustedWithCertPathValidator(chain, authType);
			checkOriginalIsCA(chain);

		} catch (CertificateException e) {
			
			checkTrusted(chain, authType);
		}

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

}
