package it.eng.idsa.dataapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "server.ssl.enabled=true", "application.ecc.protocol=https" })
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}

}
