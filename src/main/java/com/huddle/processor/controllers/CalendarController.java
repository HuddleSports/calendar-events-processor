package com.huddle.processor.controllers;

import com.huddle.processor.dao.LocationDao;
import com.huddle.processor.dao.model.Location;
import com.huddle.processor.google_calendar.CalendarService;
import com.huddle.processor.google_calendar.response.Calendar;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Log4j2
public class CalendarController {

  @Autowired
  CalendarService calendarService;

  @Autowired
  LocationDao locationDao;

  @GetMapping("/calendars")
  public List<Calendar> getCalendars() throws IOException {
    return calendarService.getCalendars()
        .stream()
        .filter(calendar -> calendar.getSummary().contains("Huddle"))
        .collect(Collectors.toList());

  }

  @PostMapping("/calendars/sync")
  public List<Calendar> syncCalendars() throws IOException {
    final List<Calendar> huddleGoogleCalendars = calendarService.getCalendars()
        .stream()
        .filter(calendar -> calendar.getSummary().contains("Huddle"))
        .collect(Collectors.toList());

    updateDBLocations(huddleGoogleCalendars);
    return huddleGoogleCalendars;
  }

  private void updateDBLocations(List<Calendar> huddleGoogleCalendars) {
    log.info("Updating db locations for huddleGoogleCalendars={}", huddleGoogleCalendars.size());
    final List<Location> dbNewLocations = huddleGoogleCalendars
        .stream()
        //Todo(Adi) from description Object Mapper
        .map(huddleGoogleCalendar -> Location
            .builder()
            .city(huddleGoogleCalendar.getSummary().split(",")[1])
            .state(huddleGoogleCalendar.getSummary().split(",")[2])
            .address(huddleGoogleCalendar.getDescription())
            .calendarId(huddleGoogleCalendar.getId())
            .build())
        .collect(Collectors.toList());
    locationDao.upsertLocations(dbNewLocations);
  }

}
