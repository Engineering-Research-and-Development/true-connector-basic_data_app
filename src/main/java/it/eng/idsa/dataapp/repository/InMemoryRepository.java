package it.eng.idsa.dataapp.repository;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnExpression("'${application.verifyCheckSum}' == 'true'")
public class InMemoryRepository implements CheckSumRepository {

	private ConcurrentHashMap<String, Object> inMemoryStorage = new ConcurrentHashMap<>();

	@Override
	public void save(String artifactId, Long value) {
		inMemoryStorage.put(artifactId, value);
	}

	@Override
	public void delete(String artifactId) {
		inMemoryStorage.remove(artifactId);
	}

	@Override
	public Long getByArtifactId(String artifactId) {
		return (Long) inMemoryStorage.get(artifactId);
	}

}
