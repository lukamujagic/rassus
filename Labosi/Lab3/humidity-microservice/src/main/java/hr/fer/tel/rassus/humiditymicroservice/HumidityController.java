package hr.fer.tel.rassus.humiditymicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.Duration;
import java.time.Instant;

@RestController
public class HumidityController {

    private final HumidityService humidityService = new HumidityService();

    private final Instant startTime = Instant.now();

    @GetMapping("/humidity")
    public String getHumidityData() {
        long activeSeconds = Duration.between(startTime, Instant.now()).getSeconds();
        return humidityService.getHumidityData(activeSeconds).toJson();
    }
}
