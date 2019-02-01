package com.huddle.processor.dao;

import com.huddle.processor.dao.model.Event;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateCrud;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class EventDao {

  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<Event> eventRowMapper;
  private final JdbcTemplateCrud<Event, Long> eventCrud;

  @Autowired
  public EventDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    eventRowMapper = JdbcTemplateMapperFactory
        .newInstance()
        .ignorePropertyNotFound()
        .newRowMapper(Event.class);
    eventCrud =
        JdbcTemplateMapperFactory
            .newInstance()
            .ignorePropertyNotFound()
            .crud(Event.class, Long.class)
            .to(jdbcTemplate, "Event");
  }

  public List<Event> getEvents(final String calendarId,
                               final String startDateIncl,
                               final String endDateExc) {
    String query = "Select * " +
        "from Event e " +
        "left join Location l on l.id = e.locationId " +
        "where " +
        "l.calendarId = ? " +
        "and startTime >= ? " +
        "and endTime < ?";

    Object[] params = new Object[]{calendarId, startDateIncl, endDateExc};

    return jdbcTemplate.query(query, params, eventRowMapper);
  }

  public void addEvents(final List<Event> events) {
    if (CollectionUtils.isEmpty(events)) {
      return;
    }
    eventCrud.create(events);
  }
}
