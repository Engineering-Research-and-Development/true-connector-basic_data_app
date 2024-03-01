package it.eng.idsa.dataapp.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TLSProvider {

	private static final Logger logger = LoggerFactory.getLogger(TLSProvider.class);

	@Value("${server.ssl.key-store}")
	private String tlsKeystorePath;

	@Value("${server.ssl.key-password}")
	private String tlsKeystorePassword;

	@Value("${server.ssl.key-alias}")
	private String tlsKeystoreAlias;

	@Value("${application.targetDirectory}")
	Path targetDirectory;

	@Value("${application.trustStoreName}")
	String trustStoreName;

	@Value("${application.trustStorePassword}")
	String trustStorePwd;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private Certificate cert;
	private KeyStore trustManagerKeyStore;
	private TrustManagerFactory trustFactory;

	@PostConstruct
	public void init() throws Exception {

		KeyStore tlsKeystore = KeyStore.getInstance("JKS");
		try (FileInputStream fis = new FileInputStream(Path.of(tlsKeystorePath).toFile())) {
			tlsKeystore.load(fis, tlsKeystorePassword.toCharArray());
		}

		KeyStore.PasswordProtection keyPasswordProtection = new KeyStore.PasswordProtection(
				tlsKeystorePassword.toCharArray());
		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) tlsKeystore.getEntry(tlsKeystoreAlias,
				keyPasswordProtection);
		privateKey = privateKeyEntry.getPrivateKey();

		cert = tlsKeystore.getCertificate(tlsKeystoreAlias);
		publicKey = cert.getPublicKey();
		loadTrustStore();
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public Certificate getCertificate() {
		return cert;
	}

	public Enumeration<String> getTruststoreAliases() {
		try {
			return trustManagerKeyStore.aliases();
		} catch (KeyStoreException e) {
			logger.error("Could not read aliases from truststrore");
		}
		return Collections.emptyEnumeration();
	}

	public TrustManager[] getTrustManagers() {
		return trustFactory.getTrustManagers();
	}

	public KeyStore getTrustManagerKeyStore() {
		return trustManagerKeyStore;
	}

	private void loadTrustStore() {
		logger.info("Loading truststore: " + trustStoreName);
		try (InputStream jksTrustStoreInputStream = Files.newInputStream(targetDirectory.resolve(trustStoreName))) {
			trustManagerKeyStore = KeyStore.getInstance("JKS");
			trustManagerKeyStore.load(jksTrustStoreInputStream, trustStorePwd.toCharArray());
			trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustFactory.init(trustManagerKeyStore);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			logger.error("Error while trying to read server truststore", e);
		}
	}
}
