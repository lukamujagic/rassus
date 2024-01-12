package hr.fer.tel.rassus.temperaturemicroservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
public class TemperatureController {
    private final TemperatureService temperatureService = new TemperatureService();

    private final Instant startTime = Instant.now();

    @GetMapping("/temperature")
    public String getHumidityData() {
        long activeSeconds = Duration.between(startTime, Instant.now()).getSeconds();
        return temperatureService.getTemperatureData(activeSeconds).toJson();
    }
}
