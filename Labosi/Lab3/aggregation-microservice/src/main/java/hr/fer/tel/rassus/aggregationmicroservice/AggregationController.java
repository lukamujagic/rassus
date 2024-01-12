package hr.fer.tel.rassus.aggregationmicroservice;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class AggregationController {
    @Autowired
    private AggregationConfig aggregationConfig = new AggregationConfig();
    private final RestTemplate restTemplate;

    private EurekaClient discoveryClient;

    private final String temperatureURL = "http://temperature-microservice";
    private final String humidityURL = "http://humidity-microservice";

    public AggregationController(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    @GetMapping("/aggregated")
    @ResponseBody
    public List<Data> getAggregatedData(){
        List<Data> aggregatedData = new ArrayList<>();

        //Temperature data
        ResponseEntity<Data> temperatureResponse = restTemplate.getForEntity(temperatureURL + "/temperature", Data.class);
        Data temperatureData = temperatureResponse.getBody();

        assert temperatureData != null;
        if(temperatureData.value() != null){
            if(Objects.equals(aggregationConfig.getTemperatureUnit(), "kelvin")){
                aggregatedData.add(new Data(temperatureData.name(), "K", temperatureData.value()+273.15));
            }
            else{
                aggregatedData.add(temperatureData);
            }
        }

        //Humidity data
        ResponseEntity<Data> humidityResponse = restTemplate.getForEntity(humidityURL + "/humidity", Data.class);
        Data humidityData = humidityResponse.getBody();

        assert humidityData != null;
        if(humidityData.value() != null){
            aggregatedData.add(humidityData);
        }

        return aggregatedData;
    }
}
