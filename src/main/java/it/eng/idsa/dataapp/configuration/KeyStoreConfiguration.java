package it.eng.idsa.dataapp.configuration;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyStoreConfiguration {
	@Value("${server.ssl.key-store}")
	private String tlsKeystorePath;

	@Value("${server.ssl.key-password}")
	private String tlsKeystorePassword;

	@Value("${server.ssl.key-alias}")
	private String tlsKeystoreAlias;

	private PrivateKey privateKey;
	private PublicKey publicKey;

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

		Certificate cert = tlsKeystore.getCertificate(tlsKeystoreAlias);
		publicKey = cert.getPublicKey();
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}
}
