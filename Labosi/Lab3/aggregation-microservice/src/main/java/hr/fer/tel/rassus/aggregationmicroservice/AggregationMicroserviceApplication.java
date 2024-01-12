package hr.fer.tel.rassus.aggregationmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class AggregationMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AggregationMicroserviceApplication.class, args);
	}


	@Bean
	public RestTemplate getRestTemplate(){
		return new RestTemplate();
	}

}
