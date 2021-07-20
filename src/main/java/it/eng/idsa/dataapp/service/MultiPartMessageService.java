package it.eng.idsa.dataapp.service;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.Message;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing MultiPartMessage.
 */
public interface MultiPartMessageService {
	//check market4.0-data_app_test_BE/src/main/resources/dataFiles/contract_agreement.json for more information
	static final String DEFAULT_CONTRACT_AGREEMENT = "https://w3id.org/idsa/autogen/contract/restrict-access-interval";
	static final String DEFAULT_TARGET_ARTIFACT = "http://w3id.org/engrd/connector/artifact/1";
	static final URI DEFAULT_CONTRACT_AGREEMENT_URI = URI.create(DEFAULT_CONTRACT_AGREEMENT);
	static final URI DEFAULT_TARGET_ARTIFACT_URI = URI.create(DEFAULT_TARGET_ARTIFACT);
	
	String getHeader(String body);
	String getPayload(String body);

	Message getMessage(String body);
	Message getMessage(Object header);
	String addToken(Message message, String token);
	HttpEntity createMultipartMessage(String header, String payload/*, String boundary, String contentType*/);
	String getToken(String message);
	String removeToken(Message message);
	String getResponseHeader(Message header);
	String getResponseHeader(String header);
	Message createRejectionMessageLocalIssues(Message header);
	Message createRejectionCommunicationLocalIssues(Message header);
	HttpEntity createMultipartMessageForm(String header, String payload, String frowardTo, ContentType ctPayload);
	Message createResultMessage(Message header);
	Message createArtifactResponseMessage(ArtifactRequestMessage header);
	Message createContractAgreementMessage(ContractRequestMessage header);

}
