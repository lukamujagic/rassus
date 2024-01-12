package hr.fer.tel.rassus.temperaturemicroservice;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class TemperatureService {
    public TemperatureData getTemperatureData(long activeSeconds) {
        int dataRow = (int) (activeSeconds % 100) + 1;

        String filePath = "readings.csv";
        try(CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath)).withSkipLines(1).build()){
            List<String[]> allLines = csvReader.readAll();
            String[] line = allLines.get(dataRow);
            String temperatureValue = line[0];
            Integer parsed;
            try{
                parsed = Integer.parseInt(temperatureValue);
            } catch (NumberFormatException e){
                parsed = null;
            }

            return new TemperatureData(parsed);
        }
        catch (IOException | NumberFormatException | CsvException e) {
            e.printStackTrace();
        }

        return null;
    }
}
