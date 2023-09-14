package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import it.eng.idsa.dataapp.domain.MessageIDS;
import it.eng.idsa.dataapp.service.impl.MessageServiceImpl;

class MessageServiceTest {

	@Mock
	MessageServiceImpl messageService;

	@BeforeEach
	public void init() {

		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetMessages() {
		List<MessageIDS> messages = messageService.getMessages();
		assertEquals(0, messages.size());
	}

	@Test
	public void testSetMessage() {
		MessageServiceImpl messageService = new MessageServiceImpl();
		String contentType = "text/plain";
		String header = "Test Header";
		String payload = "Test Payload";
		messageService.setMessage(contentType, header, payload);
		List<MessageIDS> messages = messageService.getMessages();
		assertEquals(1, messages.size());
		MessageIDS messageIDS = messages.get(0);
		assertEquals(contentType, messageIDS.getContentType());
		assertEquals(header, messageIDS.getHeader());
		assertEquals(payload, messageIDS.getPayload());
	}
}
