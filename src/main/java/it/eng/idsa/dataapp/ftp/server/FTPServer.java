package it.eng.idsa.dataapp.ftp.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.MappedKeyPairProvider;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.eng.idsa.dataapp.configuration.KeyStoreConfiguration;

@Service
public class FTPServer {
	private static final Logger logger = LoggerFactory.getLogger(FTPServer.class);

	@Value("${application.sftp.port}")
	private int port;

	@Autowired
	private KeyStoreConfiguration keyStoreConfiguration;

	@Value("${application.dataLakeDirectory}")
	String dataLakeDirectory;

	SshServer sshd;

	@PostConstruct
	public void startServer() throws IOException, URISyntaxException {
		start();
	}

	private void start() throws IOException, URISyntaxException {
		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);

		sshd.setKeyPairProvider(new MappedKeyPairProvider(loadKeyPair()));

		sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {
			@Override
			public boolean authenticate(String username, PublicKey key, ServerSession session)
					throws AsyncAuthException {
				logger.info("---------------------------------------------- {}, key {}", username, key);
				boolean comapred = KeyUtils.compareKeys(key, keyStoreConfiguration.getPublicKey());

				return comapred;
			}
		});
		sshd.setKeyboardInteractiveAuthenticator(KeyboardInteractiveAuthenticator.NONE);
		SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder().build();
		sshd.setSubsystemFactories(Collections.singletonList(factory));
		sshd.setCommandFactory(new ScpCommandFactory());

		sshd.setFileSystemFactory(new VirtualFileSystemFactory(Paths.get(dataLakeDirectory)));

		sshd.start();
		logger.info("SFTP server started");
	}

	private KeyPair loadKeyPair() {
		logger.info("Creating keypair on FTP Server side");
		PublicKey publicKey = keyStoreConfiguration.getPublicKey();
		PrivateKey key = keyStoreConfiguration.getPrivateKey();

		return new KeyPair(publicKey, key);
	}

	@PreDestroy
	public void shutdown() throws IOException {
		sshd.stop();
	}
}
