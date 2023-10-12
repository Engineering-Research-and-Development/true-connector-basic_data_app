package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.Artifact;
import de.fraunhofer.iais.eis.ArtifactBuilder;
import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorEndpointBuilder;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContentType;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.KeyType;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.PublicKeyBuilder;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.SecurityProfile;
import de.fraunhofer.iais.eis.TextRepresentationBuilder;
import de.fraunhofer.iais.eis.TextResourceBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.dataapp.service.impl.SelfDescriptionValidator;
import it.eng.idsa.multipart.processor.util.SelfDescriptionUtil;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

public class SelfDescriptionValidatorTest {

	private SelfDescriptionValidator responseHander = new SelfDescriptionValidator(true);
	
	@Test
	public void validSelfDescription() throws IOException {
		String selfDescription = new Serializer().serialize(createDefaultSelfDescription());
		assertTrue(responseHander.validateSelfDescription(selfDescription));
	}
	
	@Test
	public void invalidSelfDescription_publicKeyNotPresent() throws IOException {
		String selfDescription = new Serializer().serialize(SelfDescriptionUtil.createDefaultSelfDescription());
		assertFalse(responseHander.validateSelfDescription(selfDescription));
	}
	
	@Test
	public void invalidSelfDescription_notConnector() throws IOException {
		assertFalse(responseHander.validateSelfDescription("ABC"));
	}

	private Connector createDefaultSelfDescription() {
		return new BaseConnectorBuilder(URI.create("https://w3id.org/engrd/connector/"))
				._maintainer_(URI.create("http://connector.maintainer.com"))
				._curator_(URI.create("http://connector.curator.com"))
				._publicKey_(new PublicKeyBuilder()
						._keyType_(KeyType.RSA)
						._keyValue_("DUMM_PUBLIC_KEY_VALUE".getBytes(Charset.forName("UTF-8")))
						.build())
				._resourceCatalog_(getCatalog())
				._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
				._inboundModelVersion_(Util.asList(new String[] { UtilMessageService.MODEL_VERSION }))
				._title_(Util.asList(new TypedLiteral("Connector title")))
				._description_(Util.asList(new TypedLiteral("Connector description")))
				._outboundModelVersion_(UtilMessageService.MODEL_VERSION)
				._hasDefaultEndpoint_(new ConnectorEndpointBuilder(URI.create("http://default.endpoint.com"))
						._accessURL_(URI.create("http://default.endpoint.com"))
						.build())
				.build();
	}
	
	private List<ResourceCatalog> getCatalog() {
		Artifact defaultArtifact = new ArtifactBuilder(URI.create("http://w3id.org/engrd/connector/artifact/1"))
			._creationDate_(DateUtil.normalizedDateTime())
			.build();
		
		Resource offeredResource = (new TextResourceBuilder())
				._title_(Util.asList(new TypedLiteral("Default resource")))
				._description_(Util.asList(new TypedLiteral("Default resource description")))
				._contentType_(ContentType.SCHEMA_DEFINITION)
				._keyword_(Util.asList(new TypedLiteral("Engineering Ingegneria Informatica SpA"),
						new TypedLiteral("TRUEConnector")))
				._version_("1.0.0")._language_(Util.asList(Language.EN, Language.IT))
				._modified_(DateUtil.normalizedDateTime())
				._created_(DateUtil.normalizedDateTime())
				._contractOffer_(Util.asList(createContractOffer()))
				._representation_(Util.asList(getTextRepresentation(defaultArtifact)))
				.build();
		
		List<ResourceCatalog> catalogList = new ArrayList<>();
		ArrayList<Resource> offeredResources = new ArrayList<>();
		offeredResources.add(offeredResource);
		catalogList.add(new ResourceCatalogBuilder()._offeredResource_(offeredResources).build());
		return catalogList;
	}
	
	private ContractOffer createContractOffer() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);
		
		Constraint before = new ConstraintBuilder()
				._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
				._operator_(BinaryOperator.AFTER)
				._rightOperand_(new RdfResource(dateTime.minusDays(7).format(formatter), 
						URI.create("http://www.w3.org/2001/XMLSchema#dateTimeStamp")))
				.build();
		
		Constraint after = new ConstraintBuilder()
				._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
				._operator_(BinaryOperator.BEFORE)
				._rightOperand_(new RdfResource(dateTime.plusMonths(1).format(formatter), 
						URI.create("http://www.w3.org/2001/XMLSchema#dateTimeStamp")))
				.build();
		
		Permission permission2 = new PermissionBuilder()
				._target_(URI.create("http://w3id.org/engrd/connector/artifact/1"))
				._assignee_(Util.asList(URI.create("https://assignee.com")))
				._assigner_(Util.asList(URI.create("https://assigner.com")))
				._action_(Util.asList(Action.USE))
				._constraint_(Util.asList(before, after))
				.build();
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar xmlDate = null;
		try {
			xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			System.out.println(e);
		}
		
		return new ContractOfferBuilder()
				._consumer_(URI.create("https://consumer.com"))
				._provider_(URI.create("https://provider.com"))
				._permission_(Util.asList(permission2))
				._contractDate_(DateUtil.normalizedDateTime())
				._contractStart_(xmlDate)
				._contractEnd_(null)
				.build();
	}
	
	private Representation getTextRepresentation(Artifact artifact) {
		return new TextRepresentationBuilder()
				._created_(DateUtil.normalizedDateTime())
				._instance_(Util.asList(artifact))
				._language_(Language.EN)
				.build();
	}
}
