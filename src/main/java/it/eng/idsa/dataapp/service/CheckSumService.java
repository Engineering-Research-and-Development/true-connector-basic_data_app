package it.eng.idsa.dataapp.service;

import de.fraunhofer.iais.eis.Message;

public interface CheckSumService {

	void addCheckSum(String targetArtifact, Long value);

	Long getCheckSumByArtifactId(String targetArtifact);

	void deleteCheckSumByArtifactId(String targetArtifact);

	Long calculateCheckSum(byte[] bytes);

	String calculateCheckSumToString(String requestedArtifact, Message message);
}
