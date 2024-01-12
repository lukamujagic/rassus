package hr.fer.tel.rassus.humiditymicroservice;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Service
public class HumidityService {
    public HumidityData getHumidityData(long activeSeconds) {
        int dataRow = (int) (activeSeconds % 100) + 1;

        String filePath = "readings.csv";
        try(CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath)).withSkipLines(1).build()){
            List<String[]> allLines = csvReader.readAll();
            String[] line = allLines.get(dataRow);
            String humidityValue = line[2];
            Integer parsed;
            try{
                parsed = Integer.parseInt(humidityValue);
            } catch (NumberFormatException e){
                parsed = null;
            }

            return new HumidityData(parsed);
        }
        catch (IOException | NumberFormatException | CsvException e) {
            e.printStackTrace();
        }

        return null;
    }
}
