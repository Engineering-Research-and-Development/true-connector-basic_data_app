package it.eng.idsa.dataapp.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32C;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.eng.idsa.dataapp.service.CheckSumService;

@Service
public class CheckSumServiceImpl implements CheckSumService {

	private static final Logger logger = LoggerFactory.getLogger(CheckSumService.class);

	@Value("${application.checkSumStorage}")
	private String checkSumStorage;

	private final ConcurrentHashMap<String, Object> inMemoryStorage;

	public CheckSumServiceImpl(ConcurrentHashMap<String, Object> inMemoryStorage) {
		this.inMemoryStorage = inMemoryStorage;
	}

	@Override
	public void addCheckSum(String targetArtifact, Long value) {
		if (StringUtils.equals(checkSumStorage, "h2")) {
			logger.debug("Using h2 as persistence");
		} else {
			logger.debug("Using threadLocal as persistence");
			addNewCheckSum(targetArtifact, value);
		}
	}

	@Override
	public Long getCheckSumByArtifactId(String targetArtifact) {
		if (StringUtils.equals(checkSumStorage, "h2")) {
			logger.debug("Using h2 as persistence");
			return null;
		} else {
			logger.debug("Using threadLocal as persistence");
			return getCheckSumFromStorageByTarget(targetArtifact);
		}
	}

	@Override
	public void deleteCheckSumByArtifactId(String targetArtifact) {
		if (StringUtils.equals(checkSumStorage, "h2")) {
			logger.debug("Using h2 as persistence");
		} else {
			logger.debug("Using threadLocal as persistence");

			inMemoryStorage.remove(targetArtifact);
		}
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

	private Long getCheckSumFromStorageByTarget(String targetArtifact) {
		return (Long) inMemoryStorage.get(targetArtifact);
	}

	private void addNewCheckSum(String targetArtifact, Long value) {
		inMemoryStorage.put(targetArtifact, value);
	}

}
