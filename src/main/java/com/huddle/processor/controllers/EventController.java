/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huddle.processor.controllers;

import com.huddle.processor.dao.EventDao;
import com.huddle.processor.dao.LocationDao;
import com.huddle.processor.dao.model.Event;
import com.huddle.processor.dao.model.Location;
import com.huddle.processor.google_calendar.CalendarService;
import com.huddle.processor.google_calendar.response.Calendar;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@RestController
@Log4j2
public class EventController {

  @Autowired
  CalendarService calendarService;

  @Autowired
  LocationDao locationDao;

  @Autowired
  EventDao eventDao;

  @GetMapping("/process/events")
  public String processEvents(final String startTimeIncl,
                              final String endTimeExl,
                              final String timezoneOffset) throws IOException {
    final List<Calendar> huddleGoogleCalendars = calendarService.getCalendars()
        .stream()
        .filter(calendar -> calendar.getSummary().contains("Huddle"))
        .collect(Collectors.toList());

    updateDBLocations(huddleGoogleCalendars);
    final List<Location> dbLocations = locationDao.getLocations();
    //TODO(Adi): should do in parallel
    for (Location dbLocation: dbLocations) {
      processEvents(startTimeIncl, endTimeExl, timezoneOffset, dbLocation);
    }
    return "Events Processed!";
  }


  private void processEvents(final String startTimeIncl,
                             final String endTimeExl,
                             final String timezoneOffset,
                             final Location location) throws IOException {
    final ZonedDateTime startTime = getDateTime(startTimeIncl, timezoneOffset);
    final ZonedDateTime endTime = getDateTime(endTimeExl, timezoneOffset);
    final List<Event> events = eventDao.getEvents(location.getCalendarId(), startTime.format(ISO_LOCAL_DATE), endTime.format(ISO_LOCAL_DATE));
    if (!CollectionUtils.isEmpty(events)) {
      log.info("events={} for calendarId={} startTimeIncl={} endTimeExl={} are already present",
          events.size(), location.getCalendarId(), startTimeIncl, endTimeExl);
      return;
    }
    addNewEvents(startTimeIncl, endTimeExl, timezoneOffset, location);
  }

  private void addNewEvents(final String startTimeIncl,
                            final String endTimeExl,
                            final String timezoneOffset,
                            final Location location) throws IOException {
    final List<com.huddle.processor.google_calendar.response.Event> googleEvents =
        calendarService.getEvents(startTimeIncl, endTimeExl, timezoneOffset, location.getCalendarId());
    final List<Event> newDBEvents = googleEvents
        .stream()
        .map(googleEvent -> Event.builder()
            .locationId(location.getId())
            .description(googleEvent.getDescription())
            .organizer(googleEvent.getDescription().split(",")[0])
            .type(googleEvent.getDescription().split(",")[1].toUpperCase())
            .price(Integer.parseInt(googleEvent.getDescription().split(",")[2]))
            .startTime(convertTimeZoneTimeToLocalTimeZone(googleEvent.getStartTime()))
            .endTime(convertTimeZoneTimeToLocalTimeZone(googleEvent.getEndTime()))
            .build())
        .collect(Collectors.toList());
    eventDao.addEvents(newDBEvents);
  }

  private void updateDBLocations(List<Calendar> huddleGoogleCalendars) {
    final List<Location> dbLocations = locationDao.getLocations();
    final List<Location> dbNewLocations = huddleGoogleCalendars
        .stream()
        .filter(huddleGoogleCalendar -> !dbLocations
            .stream()
            .filter(dbLocation -> dbLocation.getCalendarId().equalsIgnoreCase(huddleGoogleCalendar.getId()))
            .findFirst()
            .isPresent())
        .map(huddleGoogleCalendar -> Location
            .builder()
            .city(huddleGoogleCalendar.getSummary().split(",")[1])
            .state(huddleGoogleCalendar.getSummary().split(",")[2])
            .address(huddleGoogleCalendar.getDescription())
            .calendarId(huddleGoogleCalendar.getId())
            .build())
        .collect(Collectors.toList());
    locationDao.addLocations(dbNewLocations);
  }

  private ZonedDateTime getDateTime(final String dateTime,
                                    final String timeZoneOffset) {
    return ZonedDateTime.parse(String.format("%s+%s", dateTime, timeZoneOffset));
  }

  private String convertTimeZoneTimeToLocalTimeZone(String timeZoneTime) {
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeZoneTime);
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return zonedDateTime.format(dateTimeFormatter);
  }
}
