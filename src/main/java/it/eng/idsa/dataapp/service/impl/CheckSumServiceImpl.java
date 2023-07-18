package it.eng.idsa.dataapp.service.impl;

import java.util.zip.CRC32C;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import it.eng.idsa.dataapp.repository.CheckSumRepository;
import it.eng.idsa.dataapp.service.CheckSumService;

@Service
@ConditionalOnExpression("'${application.verifyCheckSum}' == 'true'")
public class CheckSumServiceImpl implements CheckSumService {

	private static final Logger logger = LoggerFactory.getLogger(CheckSumService.class);

	private final CheckSumRepository checkSumRepository;

	public CheckSumServiceImpl(CheckSumRepository checkSumRepository) {
		this.checkSumRepository = checkSumRepository;
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

}
