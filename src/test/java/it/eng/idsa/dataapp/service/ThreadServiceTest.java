package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import it.eng.idsa.dataapp.service.impl.ThreadServiceImpl;

class ThreadServiceTest {

	@InjectMocks
	private ThreadServiceImpl threadService;
	static final String KEY = "key";
	static final String VALUE = "value";

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void setAndGetValueToTheadLocaltest() {

		threadService.setThreadLocalValue(KEY, VALUE);
		assertNotNull(threadService.getThreadLocalValue(KEY));
		assertEquals(threadService.getThreadLocalValue(KEY), VALUE);
	}

	@Test
	void removeValueToTheadLocaltest() {

		threadService.setThreadLocalValue(KEY, VALUE);
		threadService.removeThreadLocalValue(KEY);
		assertNull(threadService.getThreadLocalValue(KEY));
	}

	@Test
	void getNonExistendValue() {
		assertNull(threadService.getThreadLocalValue(KEY));
		assertEquals(threadService.getThreadLocalValue(KEY), null);
	}
}
