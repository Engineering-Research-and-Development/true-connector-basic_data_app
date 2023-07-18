package it.eng.idsa.dataapp.service;

public interface CheckSumService {

	void addCheckSum(String targetArtifact, Long value);

	Long getCheckSumByArtifactId(String targetArtifact);

	void deleteCheckSumByArtifactId(String targetArtifact);

	Long calculateCheckSum(byte[] bytes);

}
