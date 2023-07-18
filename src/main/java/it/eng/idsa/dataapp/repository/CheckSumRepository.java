package it.eng.idsa.dataapp.repository;

public interface CheckSumRepository {
	void save(String artifactId, Long checkSum);
	void delete(String artifactId);
	Long getByArtifactId(String artifactId);
}
