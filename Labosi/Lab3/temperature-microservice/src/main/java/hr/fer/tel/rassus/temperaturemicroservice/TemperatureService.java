package hr.fer.tel.rassus.temperaturemicroservice;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.List;

@Service
public class TemperatureService {

    private final DataSource dataSource;

    @Autowired
    public TemperatureService(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initializeTable(){

        clearTable();

        String filePath = "readings.csv";

        try(CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath)).withSkipLines(1).build()){
            List<String[]> allLines = csvReader.readAll();

            for(int i = 1; i < allLines.size(); i++){
                String temperatureValue = allLines.get(i-1)[0];

                try {
                    int parsedTemperature = Integer.parseInt(temperatureValue);
                    insertTemperatureData(i, parsedTemperature);
                }
                catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
        }
        catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private void clearTable() {
        try (Connection connection = dataSource.getConnection()) {
            String deleteQuery = "DELETE FROM temperature_data";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(deleteQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertTemperatureData(int rowId, int temperatureValue) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO temperature_data (row_id, temperature_value) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, rowId);
                preparedStatement.setInt(2, temperatureValue);
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TemperatureData getTemperatureData(long activeSeconds) {
        int dataRow = (int) (activeSeconds % 100) + 1;

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT temperature_value FROM temperature_data WHERE row_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, dataRow);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int temperatureValue = resultSet.getInt("temperature_value");
                        return new TemperatureData(temperatureValue);
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
