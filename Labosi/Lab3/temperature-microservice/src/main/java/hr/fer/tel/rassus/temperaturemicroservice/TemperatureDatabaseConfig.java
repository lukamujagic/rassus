package hr.fer.tel.rassus.temperaturemicroservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

@Configuration
public class TemperatureDatabaseConfig {

    @Bean
    public DataSource dataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:temperature_data.db");

        // Initialize the database schema
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS temperature_data (row_id INTEGER PRIMARY KEY, temperature_value INTEGER)");

        return dataSource;
    }
}
