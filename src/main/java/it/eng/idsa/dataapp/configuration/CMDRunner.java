package it.eng.idsa.dataapp.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import it.eng.idsa.dataapp.watchdir.DirectoryWatcher;

@Configuration
public class CMDRunner {
	
	@Autowired
	private DirectoryWatcher directoryWatcher;

	@Bean
	public CommandLineRunner schedulingRunner(TaskExecutor executor) {
	    return new CommandLineRunner() {
	        public void run(String... args) throws Exception {
	            executor.execute(directoryWatcher);
	        }
	    };
	}
}
