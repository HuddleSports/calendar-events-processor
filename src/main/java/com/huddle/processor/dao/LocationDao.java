package com.huddle.processor.dao;

import com.google.common.base.Preconditions;
import com.huddle.processor.dao.model.Location;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class LocationDao {

  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<Location> locationRowMapper;
  private final JdbcTemplateCrud<Location, String> locationCrud;

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
            .crud(Location.class, String.class)
            .to(jdbcTemplate, "Location");
  }

  public List<Location> getLocations() {
    String query = "Select * from Location";
    return jdbcTemplate.query(query, locationRowMapper);
  }

  public Location getLocation(String calendarId) {
    return locationCrud.read(calendarId);
  }

  public Location getLocation(int id) {
    String query = "Select * " +
        "from Location " +
        "where " +
        "id = ? ";

    Object[] params = new Object[]{id};

    List<Location> locations = jdbcTemplate.query(query, params, locationRowMapper);
    if (CollectionUtils.isEmpty(locations)) {
      log.error("id={} not found in database", id);
      return null;
    }
    return locations.get(0);
  }

  public void upsertLocations(final List<Location> locations) {
    if (CollectionUtils.isEmpty(locations)) {
      return;
    }
    locationCrud.createOrUpdate(locations);
  }
}
