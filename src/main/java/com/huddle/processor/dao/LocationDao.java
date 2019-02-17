package com.huddle.processor.dao;

import com.google.common.base.Preconditions;
import com.huddle.processor.dao.model.Location;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateCrud;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class LocationDao {

  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<Location> locationRowMapper;
  private final JdbcTemplateCrud<Location, Long> locationCrud;

  @Autowired
  public LocationDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    locationRowMapper =
        JdbcTemplateMapperFactory
            .newInstance()
            .ignorePropertyNotFound()
            .newRowMapper(Location.class);
    locationCrud =
        JdbcTemplateMapperFactory
            .newInstance()
            .ignorePropertyNotFound()
            .crud(Location.class, Long.class)
            .to(jdbcTemplate, "Location");
  }

  public List<Location> getLocations() {
    String query = "Select * from Location";
    return jdbcTemplate.query(query, locationRowMapper);
  }

  public Location getLocation(String calendarId) throws IOException {
    String query = "Select * " +
        "from Location " +
        "where " +
        "calendarId = ? ";

    Object[] params = new Object[]{calendarId};

    List<Location> locations = jdbcTemplate.query(query, params, locationRowMapper);
    if (CollectionUtils.isEmpty(locations)) {
      throw new IOException(String.format("CalendarId=%s not found in database", calendarId));
    }
    return locations.get(0);
  }

  public Location getLocation(int id) {
    return locationCrud.read((long) id);
  }

  public void upsertLocations(final List<Location> locations) {
    if (CollectionUtils.isEmpty(locations)) {
      return;
    }
    locationCrud.createOrUpdate(locations);
  }
}
