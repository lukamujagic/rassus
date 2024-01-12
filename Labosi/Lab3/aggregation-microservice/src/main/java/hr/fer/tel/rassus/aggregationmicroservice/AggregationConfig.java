package hr.fer.tel.rassus.aggregationmicroservice;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Configuration
@Component
@Scope("singleton")
public class AggregationConfig {
    @Value("${temperature.unit}")
    private String temperatureUnit;

    @PostConstruct
    public void postConstruct() {
        System.out.println("Temperature Unit: " + temperatureUnit);
    }
}
