package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementBuilder;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ContractOfferTest {

	public ContractOffer getPermissionAndTarget(Connector connector, URI permission, URI target) {
		for (ResourceCatalog resourceCatalog : connector.getResourceCatalog()) {
			for (Resource resource : resourceCatalog.getOfferedResource()) {
				for (ContractOffer co : resource.getContractOffer()) {
					for (Permission p : co.getPermission()) {
						if (p.getId().equals(permission) && p.getTarget().equals(target)) {
							System.out.println("Found permission");
							return co;
						}
					}
				}
			}
		}
		return null;
	}

	@Test
	public void testOK() throws IOException {
		Connector c = readFromFile();
		assertNotNull(c);
		URI permissionURI = URI.create("https://w3id.org/idsa/autogen/permission/e57018e7-34de-49fb-8ca9-a0a80abbac37");
		URI targetURI = URI.create("http://w3id.org/engrd/connector/artifact/1");
		ContractOffer co = getPermissionAndTarget(c,
				permissionURI,
				targetURI);
		assertNotNull(co);
		List<Permission> permissions = new ArrayList<>();
		for(Permission p: co.getPermission()) {
			if(p.getId().equals(permissionURI) && p.getTarget().equals(targetURI)) {
				permissions.add(p);
			}
		}
		ContractAgreement ca = new ContractAgreementBuilder()
				._permission_(permissions)
				._contractStart_(co.getContractStart())
				._consumer_(co.getConsumer())
				._provider_(co.getProvider())
				.build();
		assertNotNull(ca);
		System.out.println(UtilMessageService.getMessageAsString(ca));
	}
	
	@Test
	public void testNotFound() throws IOException {
		Connector c = readFromFile();
		assertNotNull(c);
		ContractOffer pp = getPermissionAndTarget(c,
				URI.create("https://w3id.org/idsa/autogen/permission/71ce6a4d-98b2-45c9-9485-262ba6459566"),
				URI.create("http://w3id.org/engrd/connector/artifact/1"));
		assertNull(pp);
	}

	private Connector readFromFile() throws IOException {
		InputStream is = this.getClass().getResourceAsStream("/sd.json");
		String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
		Serializer serializer = new Serializer();
		return serializer.deserialize(text, Connector.class);
	}
}
