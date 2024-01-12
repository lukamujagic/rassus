package hr.fer.tel.rassus.humiditymicroservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;

@Getter
public class HumidityData {
    private final String name = "Humidity";
    private final String unit = "%";
    private final Integer value;

    public HumidityData(Integer value){
        this.value = value;
    }

    @Override
    public String toString() {
        return name + ": " + value + unit;
    }
    public String toJson(){
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
