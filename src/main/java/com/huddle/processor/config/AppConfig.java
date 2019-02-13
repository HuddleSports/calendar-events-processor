package com.huddle.processor.config;

import com.google.api.services.calendar.Calendar;
import com.huddle.processor.google.api.client.extensions.jdo.JdoDataStoreFactory;
import com.huddle.processor.google_calendar.CalendarClientProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class AppConfig {
  //Todo(Adi) : shift credentials to env vars on box
  private static final String CLOUD_SQL_INSTANCE_NAME = "huddle-quick-solutions:us-central1:huddle-events-processor";
  private static final String DB_USER = "root";
  private static final String DB_PASS = "Huddle123$%";
  private static final String DB_NAME = "Huddle";

  @Bean
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();

    //Todo(Adi): Consult Hikari Optimial Config settings
    config.setJdbcUrl(String.format("jdbc:mysql:///%s", DB_NAME));
    config.setUsername(DB_USER);
    config.setPassword(DB_PASS);
    config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
    config.addDataSourceProperty("cloudSqlInstance", CLOUD_SQL_INSTANCE_NAME);
    config.addDataSourceProperty("useSSL", "false");
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(10);
    config.setConnectionTimeout(10000); // 10 seconds
    config.setIdleTimeout(600000); // 10 minutes
    config.setMaxLifetime(1800000); // 30 minutes

    DataSource dataSource = new HikariDataSource(config);
    return dataSource;
  }

  @Bean
  public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
  public Calendar getCalendar(CalendarClientProvider calendarClientProvider) throws GeneralSecurityException, IOException {
    return calendarClientProvider.get();
  }
}
