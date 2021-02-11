package it.eng.idsa.dataapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@EnableCaching
@SpringBootApplication
public class ApplicationDataApp {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationDataApp.class, args);
	}

	@Bean
	public TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor(); // Or use another one of your liking
	}
	
//	@Bean
//	public CommandLineRunner schedulingRunner(TaskExecutor executor) {
//	    return new CommandLineRunner() {
//	        public void run(String... args) throws Exception {
//	            executor.execute(directoryWatcher());
//	        }
//	    };
//	}
	
//	@Value("${application.watchdir}") String watchDirectory;
//	@Bean 
//	public DirectoryWatcher directoryWatcher() throws IOException {
//		DirectoryWatcher dw = new DirectoryWatcher(watchDirectory);
//		return dw;
//	}
	
}
