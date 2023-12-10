package hr.fer.tel.rassus.lab2.data;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.CSVReaderBuilder;
import hr.fer.tel.rassus.lab2.network.EmulatedSystemClock;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CsvReader {
    private final DataManager dataManager;
    private final EmulatedSystemClock clock;
    private final String filename;

    public CsvReader(String filename, DataManager dm, EmulatedSystemClock esc){
        this.filename = filename;
        this.dataManager = dm;
        this.clock = esc;
    }

    public Reading getNewReading() {
        long activeMillis = clock.currentTimeMillis() - clock.getStartTime();
        long activeSeconds = activeMillis / 1000;

        int lineNumber = (int) (activeSeconds % 100);
        Integer valueNO2 = getNO2Data(lineNumber);
        dataManager.addReading(activeMillis, valueNO2);

        return new Reading(activeMillis, valueNO2);
    }

    private Integer getNO2Data(int lineNumber){
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(filename)).withSkipLines(1).build()) {
            List<String[]> allLines = csvReader.readAll();
            String[] line = allLines.get(lineNumber);
            String no2Value = line[4];
            Integer parsed;
            try{
                parsed = Integer.parseInt(no2Value);
            } catch (NumberFormatException e){
                parsed = null;
            }
            return parsed;
        }
        catch (IOException | CsvException e){
            throw new RuntimeException(e);
        }
    }
}
