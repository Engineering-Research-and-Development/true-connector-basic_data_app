package it.eng.idsa.dataapp.ftp.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.RequiredServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.scp.client.DefaultScpClientCreator;
import org.apache.sshd.scp.client.ScpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.context.TestPropertySource;

import it.eng.idsa.dataapp.configuration.TLSProvider;

@TestPropertySource(locations = "classpath:application.properties") // or the path to your specific properties file

public class FTPClientTest {

	String username = "test";
	String password = "password";
	String host = "localhost";
	int port = 2222;
	long defaultTimeoutSeconds = 100l;

	@Mock
	private TLSProvider keyStoreConfigurationMock;

	@InjectMocks
	private FTPClient ftpClient;

	PublicKey publicKey;
	PrivateKey privateKey;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		publicKey = mock(PublicKey.class);
		privateKey = mock(PrivateKey.class);

		// Mock the behavior of keyStoreConfiguration
		when(keyStoreConfigurationMock.getPublicKey()).thenReturn(publicKey);
		when(keyStoreConfigurationMock.getPrivateKey()).thenReturn(privateKey);
	}

//	@Test
	public void clientTest() throws Exception {
		ftpClient.downloadArtifact("README.md");
	}

//	@Test
	public void listFolderStructure() throws Exception {

		Instant start = Instant.now(); // Start time measurement

		SshClient client = SshClient.setUpDefaultClient();
//		client.setKeyIdentityProvider(new SimpleGeneratorHostKeyProvider(Path.of(ClassLoader.getSystemResource("execution_core_container.pkcs8").toURI())));
		KeyPair keyPair = loadKeyPair("classpath:ssl-server.jks");
		client.addPublicKeyIdentity(keyPair);
		client.addPasswordIdentity("changeit");
//		client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
//		client.setServerKeyVerifier(new RequiredServerKeyVerifier(publicKey));

//        client.setServerKeyVerifier(new ServerKeyVerifier() {
//			@Override
//			public boolean verifyServerKey(ClientSession clientSession, SocketAddress remoteAddress, PublicKey serverKey) {
//				System.out.println("aaaaaaaaaaaa");
//				return false;
//			}
//		});

		client.setServerKeyVerifier(new RequiredServerKeyVerifier(publicKey));
		client.setKeyIdentityProvider(KeyIdentityProvider.wrapKeyPairs(new KeyPair(publicKey, privateKey)));
		client.start();

		try (ClientSession session = client.connect(username, host, port)
				.verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {

			session.addPublicKeyIdentity(keyPair);
			session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

			try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
					ByteArrayOutputStream errorResponseStream = new ByteArrayOutputStream()) {

				ScpClient scpClient = DefaultScpClientCreator.INSTANCE.createScpClient(session);
				scpClient.download("README.md",
//								"AI-REGIO.zip",
						Paths.get("/home/mare/sftp/client"),
//							   ScpClient.Option.Recursive,
						ScpClient.Option.PreserveAttributes, ScpClient.Option.TargetIsDirectory);

				String errorString = new String(errorResponseStream.toByteArray(), StandardCharsets.UTF_8);
				if (!errorString.isEmpty()) {
					System.out.println(errorString);
					throw new Exception(errorString);
				}
				String responseString = new String(responseStream.toByteArray());
				System.out.println("Response: " + responseString);
			}
		} finally {
			client.stop();
			Instant finish = Instant.now(); // End time measurement
			long timeElapsed = Duration.between(start, finish).toMillis(); // Calculate duration in milliseconds
			System.out.println("Time taken: " + timeElapsed + " ms");
		}
	}

	private KeyPair loadKeyPair(String path) throws KeyStoreException, CertificateException, NoSuchAlgorithmException,
			IOException, UnrecoverableKeyException, NoSuchProviderException {
		KeyStore p12 = KeyStore.getInstance("JKS");
//	    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
		InputStream is = new DefaultResourceLoader().getResource(path).getInputStream();
		p12.load(is, "changeit".toCharArray());
		java.security.cert.Certificate cert = p12.getCertificate("execution-core-container");
		publicKey = cert.getPublicKey();
		privateKey = (PrivateKey) p12.getKey("execution-core-container", "changeit".toCharArray());
		System.out.println("publicKey: " + publicKey);
		System.out.println("privateKey: " + privateKey);
		return new KeyPair(publicKey, privateKey);
	}
}
