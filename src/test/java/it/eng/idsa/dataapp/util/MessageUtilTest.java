package it.eng.idsa.dataapp.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.MultiPartMessageService;

public class MessageUtilTest {
	
	private MessageUtil messageUtil;
	
	@Mock
	private Path dataLakeDirectory;
	
	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private ECCProperties eccProperties;
	
	@Mock
	private MultiPartMessageService multiPartMessageService;
	
	private HttpHeaders headers;
	
	private Serializer serializer = new Serializer();
	
	private static final String IDS_MESSAGE_TYPE = "IDS-MessageType";
	private static final String IDS_REQUESTED_ELEMENT = "IDS-RequestedElement";
	
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(eccProperties.getHost()).thenReturn("localhost");
		when(restTemplate.getForObject(any(), any())).thenReturn(getMockSelfDescription());
		dataLakeDirectory = Path.of("/dataLakeDirectory");
		messageUtil = new MessageUtil(dataLakeDirectory, restTemplate, eccProperties, multiPartMessageService);
		headers = new HttpHeaders();
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithoutRequestedElementMethodParameterMessageSuccessfull() {
 		String payload = messageUtil.createResponsePayload(TestUtilMessageService.getDescriptionRequestMessageWithoutRequestedElement());
		assertEquals(getMockSelfDescription(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithoutRequestedElementMethodParameterMessageFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
 		String payload = messageUtil.createResponsePayload(TestUtilMessageService.getDescriptionRequestMessageWithoutRequestedElement());
		assertNotEquals(getMockSelfDescription(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithoutRequestedElementMethodParameterStringSuccessfull() {
 		String payload = messageUtil.createResponsePayload(TestUtilMessageService.getMessageAsString(TestUtilMessageService.getDescriptionRequestMessageWithoutRequestedElement()));
		assertEquals(getMockSelfDescription(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithoutRequestedElementMethodParameterStringFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
 		String payload = messageUtil.createResponsePayload(TestUtilMessageService.getMessageAsString(TestUtilMessageService.getDescriptionRequestMessageWithoutRequestedElement()));
		assertNotEquals(getMockSelfDescription(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithoutRequestedElementMethodParameterHttpHeadersSuccessfull() {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
 		String payload = messageUtil.createResponsePayload(headers);
		assertEquals(getMockSelfDescription(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithoutRequestedElementMethodParameterHttpHeadersFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(null);
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
 		String payload = messageUtil.createResponsePayload(headers);
		assertNotEquals(getMockSelfDescription(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithRequestedElementMethodParameterMessageSuccessfull() throws IOException {
 		String payload = messageUtil.createResponsePayload(TestUtilMessageService.getDescriptionRequestMessageWithRequestedElement());
		assertEquals(serializer.serialize(serializer.deserialize(getMockRequestedElement(), Resource.class)), serializer.serialize(serializer.deserialize(payload, Resource.class)));
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithRequestedElementMethodParameterMessageFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(getMockSelfDescriptionWithWrongRequestedElement());
 		String payload = messageUtil.createResponsePayload(TestUtilMessageService.getDescriptionRequestMessageWithRequestedElement());
		assertNotEquals(getMockRequestedElement(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithRequestedElementMethodParameterStringSuccessfull() throws IOException {
		DescriptionRequestMessage drm = TestUtilMessageService.getDescriptionRequestMessageWithRequestedElement();
		String drmString = TestUtilMessageService.getMessageAsString(drm);
		when(multiPartMessageService.getMessage((Object)drmString)).thenReturn(drm);
 		String payload = messageUtil.createResponsePayload(drmString);
 		//deserializing and serializing again is needed for the objects tu be asserted as true, because of the order of fields
		assertEquals(serializer.serialize(serializer.deserialize(getMockRequestedElement(), Resource.class)), serializer.serialize(serializer.deserialize(payload, Resource.class)));
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithRequestedElementMethodParameterStringFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(getMockSelfDescriptionWithWrongRequestedElement());
		DescriptionRequestMessage drm = TestUtilMessageService.getDescriptionRequestMessageWithRequestedElement();
		String drmString = TestUtilMessageService.getMessageAsString(drm);
		when(multiPartMessageService.getMessage((Object)drmString)).thenReturn(drm);
 		String payload = messageUtil.createResponsePayload(drmString);
		assertNotEquals(getMockRequestedElement(), payload);
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithRequestedElementMethodParameterHttpHeadersSuccessfull() throws IOException {
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, "https://resource.com/11");
 		String payload = messageUtil.createResponsePayload(headers);
		assertEquals(serializer.serialize(serializer.deserialize(getMockRequestedElement(), Resource.class)), serializer.serialize(serializer.deserialize(payload, Resource.class)));
	}
	
	@Test
	public void createResponsePayloadFromDescriptionRequestMessageWithRequestedElementMethodParameterHttpHeadersFailed() {
		when(restTemplate.getForObject(any(), any())).thenReturn(getMockSelfDescriptionWithWrongRequestedElement());
		headers.add(IDS_MESSAGE_TYPE, DescriptionRequestMessage.class.getSimpleName());
		headers.add(IDS_REQUESTED_ELEMENT, "https://resource.com/11");
 		String payload = messageUtil.createResponsePayload(headers);
		assertNotEquals(getMockRequestedElement(), payload);
	}
	
	

	
	
	private String getMockRequestedElement() {
		return "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:DataResource\",\r\n" + 
				"  \"@id\" : \"https://resource.com/11\",\r\n" + 
				"  \"ids:language\" : [ {\r\n" + 
				"    \"@id\" : \"idsc:EN\"\r\n" + 
				"  }, {\r\n" + 
				"    \"@id\" : \"idsc:IT\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:version\" : \"1.0.0\",\r\n" + 
				"  \"ids:created\" : {\r\n" + 
				"    \"@value\" : \"2021-06-30T16:13:04.499+02:00\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:contentType\" : {\r\n" + 
				"    \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:keyword\" : [ {\r\n" + 
				"    \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  }, {\r\n" + 
				"    \"@value\" : \"broker\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  }, {\r\n" + 
				"    \"@value\" : \"trueConnector\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:description\" : [ {\r\n" + 
				"    \"@value\" : \"Resource description\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:title\" : [ {\r\n" + 
				"    \"@value\" : \"Resource title\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:contractOffer\" : [ {\r\n" + 
				"    \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"    \"@id\" : \"https://contract.com/11\",\r\n" + 
				"    \"ids:permission\" : [ {\r\n" + 
				"      \"@type\" : \"ids:Permission\",\r\n" + 
				"      \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"      \"ids:target\" : {\r\n" + 
				"        \"@id\" : \"http://w3id.org/engrd/connector/artifact/11\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:action\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:USE\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:assigner\" : [ {\r\n" + 
				"        \"@id\" : \"https://assigner.com\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:assignee\" : [ {\r\n" + 
				"        \"@id\" : \"https://assignee.com\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:constraint\" : [ {\r\n" + 
				"        \"@type\" : \"ids:Constraint\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/constraint/897de23c-e4a0-4f2a-ab61-8d12e843be7e\",\r\n" + 
				"        \"ids:operator\" : {\r\n" + 
				"          \"@id\" : \"idsc:AFTER\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:leftOperand\" : {\r\n" + 
				"          \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:rightOperand\" : {\r\n" + 
				"          \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"          \"@type\" : \"xsd:datetime\"\r\n" + 
				"        }\r\n" + 
				"      }, {\r\n" + 
				"        \"@type\" : \"ids:Constraint\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/constraint/ae279fc3-bb94-4017-8095-a56f82fada19\",\r\n" + 
				"        \"ids:operator\" : {\r\n" + 
				"          \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:leftOperand\" : {\r\n" + 
				"          \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:rightOperand\" : {\r\n" + 
				"          \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"          \"@type\" : \"xsd:datetime\"\r\n" + 
				"        }\r\n" + 
				"      } ]\r\n" + 
				"    } ],\r\n" + 
				"    \"ids:provider\" : {\r\n" + 
				"      \"@id\" : \"https://provider.com\"\r\n" + 
				"    },\r\n" + 
				"    \"ids:contractDate\" : {\r\n" + 
				"      \"@value\" : \"2021-06-30T16:13:04.492+02:00\",\r\n" + 
				"      \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"    },\r\n" + 
				"    \"ids:consumer\" : {\r\n" + 
				"      \"@id\" : \"https://consumer.com\"\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:representation\" : [ {\r\n" + 
				"    \"@type\" : \"ids:DataRepresentation\",\r\n" + 
				"    \"@id\" : \"https://w3id.org/idsa/autogen/dataRepresentation/2bba1180-905e-4ca1-a9de-4b2004faad7f\",\r\n" + 
				"    \"ids:instance\" : [ {\r\n" + 
				"      \"@type\" : \"ids:Artifact\",\r\n" + 
				"      \"@id\" : \"http://w3id.org/engrd/connector/artifact/11\",\r\n" + 
				"      \"ids:fileName\" : \"some_file_11.pdf\",\r\n" + 
				"      \"ids:creationDate\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.481+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      }\r\n" + 
				"    } ],\r\n" + 
				"    \"ids:created\" : {\r\n" + 
				"      \"@value\" : \"2021-06-30T16:13:04.484+02:00\",\r\n" + 
				"      \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:sovereign\" : {\r\n" + 
				"    \"@id\" : \"https://sovereign.com\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:modified\" : {\r\n" + 
				"    \"@value\" : \"2021-06-30T16:13:04.499+02:00\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"  }\r\n" + 
				"}";
	}

	private String getMockSelfDescription() {
		return "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:BaseConnector\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/engrd/connector/\",\r\n" + 
				"  \"ids:description\" : [ {\r\n" + 
				"    \"@value\" : \"Sender Connector description\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:title\" : [ {\r\n" + 
				"    \"@value\" : \"Sender Connector title\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:maintainer\" : {\r\n" + 
				"    \"@id\" : \"http://sender.maintainerURI.com\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:curator\" : {\r\n" + 
				"    \"@id\" : \"http://sender.curatorURI.com\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:inboundModelVersion\" : [ \"@information.model.version@\" ],\r\n" + 
				"  \"ids:hasDefaultEndpoint\" : {\r\n" + 
				"    \"@type\" : \"ids:ConnectorEndpoint\",\r\n" + 
				"    \"@id\" : \"https://192.168.56.1:8443/\",\r\n" + 
				"    \"ids:accessURL\" : {\r\n" + 
				"      \"@id\" : \"https://192.168.56.1:8443/\"\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"ids:outboundModelVersion\" : \"@information.model.version@\",\r\n" + 
				"  \"ids:resourceCatalog\" : [ {\r\n" + 
				"    \"@type\" : \"ids:ResourceCatalog\",\r\n" + 
				"    \"@id\" : \"http://catalog1.com\",\r\n" + 
				"    \"ids:offeredResource\" : [ {\r\n" + 
				"      \"@type\" : \"ids:DataResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/11\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.499+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.499+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/11\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/11\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/897de23c-e4a0-4f2a-ab61-8d12e843be7e\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/ae279fc3-bb94-4017-8095-a56f82fada19\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.492+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:DataRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/dataRepresentation/2bba1180-905e-4ca1-a9de-4b2004faad7f\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/11\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_11.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.481+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.484+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    }, {\r\n" + 
				"      \"@type\" : \"ids:ImageResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/12\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.506+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.506+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/12\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/12\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/897de23c-e4a0-4f2a-ab61-8d12e843be7e\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/ae279fc3-bb94-4017-8095-a56f82fada19\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.503+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ImageRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/imageRepresentation/1e937887-5b53-4dea-abec-3b261c698e82\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/12\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_12.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.500+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.502+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    } ]\r\n" + 
				"  }, {\r\n" + 
				"    \"@type\" : \"ids:ResourceCatalog\",\r\n" + 
				"    \"@id\" : \"http://catalog2.com\",\r\n" + 
				"    \"ids:offeredResource\" : [ {\r\n" + 
				"      \"@type\" : \"ids:DataResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/21\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/21\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/21\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/523222d8-ae9e-4833-b30b-d7bc36972ebe\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/00cfb00e-2216-4778-9646-49f52cdb170a\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:DataRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/dataRepresentation/8bee312b-93a1-4f2d-bde5-dc4005d3aa17\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/21\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_21.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    }, {\r\n" + 
				"      \"@type\" : \"ids:ImageResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/22\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/22\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/22\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/523222d8-ae9e-4833-b30b-d7bc36972ebe\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/00cfb00e-2216-4778-9646-49f52cdb170a\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ImageRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/imageRepresentation/a5e7eacf-88ce-48c2-8de9-628bec651e5f\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/22\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_22.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    } ]\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:securityProfile\" : {\r\n" + 
				"    \"@id\" : \"idsc:BASE_SECURITY_PROFILE\"\r\n" + 
				"  }\r\n" + 
				"}";
	}
	
	private String getMockSelfDescriptionWithWrongRequestedElement() {
		return "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:BaseConnector\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/engrd/connector/\",\r\n" + 
				"  \"ids:description\" : [ {\r\n" + 
				"    \"@value\" : \"Sender Connector description\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:title\" : [ {\r\n" + 
				"    \"@value\" : \"Sender Connector title\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:maintainer\" : {\r\n" + 
				"    \"@id\" : \"http://sender.maintainerURI.com\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:curator\" : {\r\n" + 
				"    \"@id\" : \"http://sender.curatorURI.com\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:inboundModelVersion\" : [ \"@information.model.version@\" ],\r\n" + 
				"  \"ids:hasDefaultEndpoint\" : {\r\n" + 
				"    \"@type\" : \"ids:ConnectorEndpoint\",\r\n" + 
				"    \"@id\" : \"https://192.168.56.1:8443/\",\r\n" + 
				"    \"ids:accessURL\" : {\r\n" + 
				"      \"@id\" : \"https://192.168.56.1:8443/\"\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"ids:outboundModelVersion\" : \"@information.model.version@\",\r\n" + 
				"  \"ids:resourceCatalog\" : [ {\r\n" + 
				"    \"@type\" : \"ids:ResourceCatalog\",\r\n" + 
				"    \"@id\" : \"http://catalog1.com\",\r\n" + 
				"    \"ids:offeredResource\" : [ {\r\n" + 
				"      \"@type\" : \"ids:DataResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/13\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.499+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.499+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/11\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/11\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/897de23c-e4a0-4f2a-ab61-8d12e843be7e\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/ae279fc3-bb94-4017-8095-a56f82fada19\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.492+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:DataRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/dataRepresentation/2bba1180-905e-4ca1-a9de-4b2004faad7f\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/11\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_11.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.481+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.484+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    }, {\r\n" + 
				"      \"@type\" : \"ids:ImageResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/12\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.506+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.506+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/12\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/12\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/897de23c-e4a0-4f2a-ab61-8d12e843be7e\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/ae279fc3-bb94-4017-8095-a56f82fada19\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.503+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ImageRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/imageRepresentation/1e937887-5b53-4dea-abec-3b261c698e82\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/12\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_12.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.500+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.502+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    } ]\r\n" + 
				"  }, {\r\n" + 
				"    \"@type\" : \"ids:ResourceCatalog\",\r\n" + 
				"    \"@id\" : \"http://catalog2.com\",\r\n" + 
				"    \"ids:offeredResource\" : [ {\r\n" + 
				"      \"@type\" : \"ids:DataResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/21\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/21\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/21\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/523222d8-ae9e-4833-b30b-d7bc36972ebe\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/00cfb00e-2216-4778-9646-49f52cdb170a\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:DataRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/dataRepresentation/8bee312b-93a1-4f2d-bde5-dc4005d3aa17\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/21\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_21.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    }, {\r\n" + 
				"      \"@type\" : \"ids:ImageResource\",\r\n" + 
				"      \"@id\" : \"https://resource.com/22\",\r\n" + 
				"      \"ids:language\" : [ {\r\n" + 
				"        \"@id\" : \"idsc:EN\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@id\" : \"idsc:IT\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:version\" : \"1.0.0\",\r\n" + 
				"      \"ids:created\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:description\" : [ {\r\n" + 
				"        \"@value\" : \"Resource description\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:contentType\" : {\r\n" + 
				"        \"@id\" : \"idsc:SCHEMA_DEFINITION\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:keyword\" : [ {\r\n" + 
				"        \"@value\" : \"Engineering Ingegneria Informatica SpA\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"broker\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      }, {\r\n" + 
				"        \"@value\" : \"trueConnector\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:modified\" : {\r\n" + 
				"        \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:contractOffer\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"        \"@id\" : \"https://contract.com/22\",\r\n" + 
				"        \"ids:permission\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Permission\",\r\n" + 
				"          \"@id\" : \"http://example.com/policy/restrict-access-interval\",\r\n" + 
				"          \"ids:target\" : {\r\n" + 
				"            \"@id\" : \"http://w3id.org/engrd/connector/artifact/22\"\r\n" + 
				"          },\r\n" + 
				"          \"ids:constraint\" : [ {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/523222d8-ae9e-4833-b30b-d7bc36972ebe\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2020-10-01T00:00:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:AFTER\"\r\n" + 
				"            }\r\n" + 
				"          }, {\r\n" + 
				"            \"@type\" : \"ids:Constraint\",\r\n" + 
				"            \"@id\" : \"https://w3id.org/idsa/autogen/constraint/00cfb00e-2216-4778-9646-49f52cdb170a\",\r\n" + 
				"            \"ids:rightOperand\" : {\r\n" + 
				"              \"@value\" : \"2021-31-12T23:59:00Z\",\r\n" + 
				"              \"@type\" : \"xsd:datetime\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:leftOperand\" : {\r\n" + 
				"              \"@id\" : \"idsc:POLICY_EVALUATION_TIME\"\r\n" + 
				"            },\r\n" + 
				"            \"ids:operator\" : {\r\n" + 
				"              \"@id\" : \"idsc:BEFORE\"\r\n" + 
				"            }\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:action\" : [ {\r\n" + 
				"            \"@id\" : \"idsc:USE\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assignee\" : [ {\r\n" + 
				"            \"@id\" : \"https://assignee.com\"\r\n" + 
				"          } ],\r\n" + 
				"          \"ids:assigner\" : [ {\r\n" + 
				"            \"@id\" : \"https://assigner.com\"\r\n" + 
				"          } ]\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:provider\" : {\r\n" + 
				"          \"@id\" : \"https://provider.com\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:contractDate\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        },\r\n" + 
				"        \"ids:consumer\" : {\r\n" + 
				"          \"@id\" : \"https://consumer.com\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:sovereign\" : {\r\n" + 
				"        \"@id\" : \"https://sovereign.com\"\r\n" + 
				"      },\r\n" + 
				"      \"ids:representation\" : [ {\r\n" + 
				"        \"@type\" : \"ids:ImageRepresentation\",\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/autogen/imageRepresentation/a5e7eacf-88ce-48c2-8de9-628bec651e5f\",\r\n" + 
				"        \"ids:instance\" : [ {\r\n" + 
				"          \"@type\" : \"ids:Artifact\",\r\n" + 
				"          \"@id\" : \"http://w3id.org/engrd/connector/artifact/22\",\r\n" + 
				"          \"ids:fileName\" : \"some_file_22.pdf\",\r\n" + 
				"          \"ids:creationDate\" : {\r\n" + 
				"            \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"            \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"          }\r\n" + 
				"        } ],\r\n" + 
				"        \"ids:created\" : {\r\n" + 
				"          \"@value\" : \"2021-06-30T16:13:04.507+02:00\",\r\n" + 
				"          \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"        }\r\n" + 
				"      } ],\r\n" + 
				"      \"ids:title\" : [ {\r\n" + 
				"        \"@value\" : \"Resource title\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"      } ]\r\n" + 
				"    } ]\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:securityProfile\" : {\r\n" + 
				"    \"@id\" : \"idsc:BASE_SECURITY_PROFILE\"\r\n" + 
				"  }\r\n" + 
				"}";
	}

}
