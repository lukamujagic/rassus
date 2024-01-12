package hr.fer.tel.rassus.humiditymicroservice;

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
public class HumidityService {

    private final DataSource dataSource;

    @Autowired
    public HumidityService(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initializeTable(){

        clearTable();

        String filePath = "readings.csv";

        try(CSVReader csvReader = new CSVReaderBuilder(new FileReader(filePath)).withSkipLines(1).build()){
            List<String[]> allLines = csvReader.readAll();

            for(int i = 1; i < allLines.size(); i++){
                String humidityValue = allLines.get(i-1)[2];

                try {
                    int parsedHumidity = Integer.parseInt(humidityValue);
                    insertHumidityData(i, parsedHumidity);
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
            String deleteQuery = "DELETE FROM humidity_data";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(deleteQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertHumidityData(int rowId, int humidityValue) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO humidity_data (row_id, humidity_value) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, rowId);
                preparedStatement.setInt(2, humidityValue);
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HumidityData getHumidityData(long activeSeconds) {
        int dataRow = (int) (activeSeconds % 100) + 1;

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT humidity_value FROM humidity_data WHERE row_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, dataRow);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int humidityValue = resultSet.getInt("humidity_value");
                        return new HumidityData(humidityValue);
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
