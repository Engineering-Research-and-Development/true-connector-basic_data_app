package it.eng.idsa.dataapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32C;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.repository.CheckSumRepository;
import it.eng.idsa.dataapp.service.CheckSumService;
import it.eng.idsa.dataapp.web.rest.exceptions.NotFoundException;

@Service
@ConditionalOnExpression("'${application.verifyCheckSum}' == 'true'")
public class CheckSumServiceImpl implements CheckSumService {

	private static final Logger logger = LoggerFactory.getLogger(CheckSumService.class);

	private final CheckSumRepository checkSumRepository;
	private Path dataLakeDirectory;

	public CheckSumServiceImpl(CheckSumRepository checkSumRepository,
			@Value("${application.dataLakeDirectory}") Path dataLakeDirectory) {
		this.checkSumRepository = checkSumRepository;
		this.dataLakeDirectory = dataLakeDirectory;

	}

	@Override
	public void addCheckSum(String targetArtifact, Long value) {
		logger.info("Adding checkSum to storage...");
		checkSumRepository.save(targetArtifact, value);

	}

	@Override
	public Long getCheckSumByArtifactId(String targetArtifact) {
		logger.info("Getting checkSum from storage...");

		return checkSumRepository.getByArtifactId(targetArtifact);
	}

	@Override
	public void deleteCheckSumByArtifactId(String targetArtifact) {
		logger.info("Deleting checkSum from storage...");

		checkSumRepository.delete(targetArtifact);
	}

	@Override
	public Long calculateCheckSum(byte[] bytes) {

		if (bytes == null) {
			return 0L;
		}

		final var checksum = new CRC32C();
		checksum.update(bytes, 0, bytes.length);
		return checksum.getValue();
	}

	@Override
	public String calculateCheckSumToString(String requestedArtifact, Message message) {

		Path filePath = dataLakeDirectory.resolve(requestedArtifact);
		CRC32C checksum = new CRC32C();

		try (InputStream fis = Files.newInputStream(filePath)) {
			byte[] byteArray = new byte[8096];
			int bytesCount;

			while ((bytesCount = fis.read(byteArray)) != -1) {
				checksum.update(byteArray, 0, bytesCount);
			}
		} catch (IOException e) {
			logger.error("Could't read the file {} from datalake", requestedArtifact);

			throw new NotFoundException("Could't read the file from datalake", message);
		}

		return String.valueOf(checksum.getValue());
	}
}
