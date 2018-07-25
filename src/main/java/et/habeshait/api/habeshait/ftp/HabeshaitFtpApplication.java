package et.habeshait.api.habeshait.ftp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HabeshaitFtpApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabeshaitFtpApplication.class, args);
	}
}
