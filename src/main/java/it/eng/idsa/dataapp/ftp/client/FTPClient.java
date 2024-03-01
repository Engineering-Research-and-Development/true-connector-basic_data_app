package it.eng.idsa.dataapp.ftp.client;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.RequiredServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.scp.client.DefaultScpClientCreator;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.common.ScpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import it.eng.idsa.dataapp.configuration.TLSProvider;
import it.eng.idsa.dataapp.web.rest.exceptions.CertificateMissingException;

@Service
public class FTPClient {

	private static final Logger logger = LoggerFactory.getLogger(FTPClient.class);

	@Value("${server.ssl.key-password}")
	private String tlsKeystorePassword;

	@Value("${application.dataLakeDirectory}")
	String dataLakeDirectory;

	@Value("${application.sftp.connectorId}")
	private String username;

	@Value("${application.sftp.host}")
	private String host;

	@Value("${application.sftp.port}")
	private int port;

	@Value("${application.sftp.defaultTimeoutSeconds}")
	private long defaultTimeoutSeconds;

	@Autowired
	private TLSProvider keyStoreConfiguration;

	private KeyPair loadKeyPair() {
		logger.info("Creating keypair on FTP Client side");
		PublicKey publicKey = keyStoreConfiguration.getPublicKey();
		PrivateKey key = keyStoreConfiguration.getPrivateKey();

		return new KeyPair(publicKey, key);
	}

	public boolean downloadArtifact(String artifact) throws Exception {
		SshClient client = SshClient.setUpDefaultClient();
		client.addPublicKeyIdentity(loadKeyPair());
		client.addPasswordIdentity(tlsKeystorePassword);
		client.setServerKeyVerifier(new RequiredServerKeyVerifier(keyStoreConfiguration.getPublicKey()));
		client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(
				new KeyPair(keyStoreConfiguration.getPublicKey(), keyStoreConfiguration.getPrivateKey())));
		client.start();

		try (ClientSession session = client.connect(username, host, port)
				.verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {
			session.addPublicKeyIdentity(loadKeyPair());
			session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

			try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
					ByteArrayOutputStream errorResponseStream = new ByteArrayOutputStream()) {

				ScpClient scpClient = DefaultScpClientCreator.INSTANCE.createScpClient(session);
				try {
					scpClient.download(artifact, dataLakeDirectory, ScpClient.Option.PreserveAttributes,
							ScpClient.Option.TargetIsDirectory);
					logger.info("Connecting to SFTP server: " + host + port + "...");
				} catch (ScpException e) {
					logger.error("SCP download failed: ", e);
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Error while downloading the file, check logs for more details" + e);
				}

				String errorString = new String(errorResponseStream.toByteArray(), StandardCharsets.UTF_8);
				if (!errorString.isEmpty()) {
					logger.error("Following error occurred: ", errorString);
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Error while processing request, check logs for more details" + errorString);
				}
				logger.info("Artifact: " + artifact + " fetched from SFTP server: " + host + port);
				return true;
			}
		} catch (SshException e) {
			if (e.getMessage().contains("No more authentication methods available")) {
				logger.error("Client certificate not present in TrustStore.", e);
				throw new CertificateMissingException("Client certificate not present in TrustStore.");
			} else {
				logger.error("SSH error occurred: ", e);
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in downloadArtifact.");
			}
		} catch (Exception e) {
			logger.error("Error in downloadArtifact: ", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in downloadArtifact.");
		} finally {
			if (client != null) {
				client.stop();
			}
		}
	}
}
