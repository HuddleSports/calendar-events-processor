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
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Log4j2
public class EventController {

  @Autowired
  CalendarService calendarService;

  @Autowired
  EventDao eventDao;

  @Autowired
  LocationDao locationDao;

  @GetMapping("/process/events")
  public void processEvents(final @NonNull String startTimeIncl,
                            final String endTimeExl,
                            final @NonNull String timezoneOffset,
                            final Integer locationId,
                            final Boolean registerCallback) throws IOException {
    final List<Location> dbLocations;
    if (Objects.isNull(locationId)) {
      dbLocations = locationDao.getLocations();
    } else {
      dbLocations = Collections.singletonList(locationDao.getLocation(locationId));
    }
    processEvents(startTimeIncl, endTimeExl, timezoneOffset, dbLocations, registerCallback);
  }

  public void processEvent(final String eventId,
                           final String calendarId) throws IOException {
    Location dbLocation = locationDao.getLocation(calendarId);
    com.huddle.processor.google_calendar.response.Event googleEvent =
        calendarService.getEvent(calendarId, eventId);
    eventDao.upsertEvent(createEvent(googleEvent, dbLocation));
  }

  private String processEvents(final String startTimeIncl,
                               final String endTimeExl,
                               final String timezoneOffset,
                               final List<Location> dbLocations,
                               final Boolean registerCallback) throws IOException {
    log.info("Starting processing of events for startTimeIncl={} endTimeExl={} timezoneOffset={}, registerCallback={}",
        startTimeIncl, endTimeExl, timezoneOffset, registerCallback);
    //TODO(Adi): should do in parallel
    for (Location dbLocation : dbLocations) {
      processEvents(startTimeIncl, endTimeExl, timezoneOffset, dbLocation, registerCallback);
    }
    return "Events Processed!";
  }

  private void processEvents(final String startTimeIncl,
                             final String endTimeExl,
                             final String timezoneOffset,
                             final Location location,
                             final Boolean registerCallback) throws IOException {
    final List<com.huddle.processor.google_calendar.response.Event> googleEvents =
        calendarService.getEvents(startTimeIncl, endTimeExl, timezoneOffset, location.getCalendarId());
    final List<Event> newDBEvents = googleEvents
        .stream()
        .map(googleEvent -> createEvent(googleEvent, location))
        .collect(Collectors.toList());
    eventDao.upsertEvents(newDBEvents);
    if (registerCallback) {
      calendarService.watchEvents(startTimeIncl, endTimeExl, timezoneOffset, location.getCalendarId());
    }
  }

  private Event createEvent(final com.huddle.processor.google_calendar.response.Event googleEvent,
                            final Location location) {
    try {
      Event.EventBuilder builder = Event.builder()
          .locationId(location.getId())
          .description(googleEvent.getDescription())
          .startTime(convertTimeZoneTimeToLocalTimeZone(googleEvent.getStartTime()))
          .endTime(convertTimeZoneTimeToLocalTimeZone(googleEvent.getEndTime()))
          .calendarEventId(googleEvent.getCalendarEventId());
      String[] descriptionParts = googleEvent.getDescription().split(",");
      if (descriptionParts != null) {
        if (descriptionParts.length > 0 && descriptionParts[0] != null) {
          builder.organizer(descriptionParts[0]);
        }
        if (descriptionParts.length > 1 && descriptionParts[1] != null) {
          builder.type(descriptionParts[1].toUpperCase());
        }
        if (descriptionParts.length > 2 && descriptionParts[2] != null) {
          try {
            builder.price(Integer.parseInt(descriptionParts[2]));
          } catch (Exception e) {
            log.error("Failed to set price for description={}", googleEvent.getDescription(), e);
          }
        }
      }
      return builder.build();
    } catch (Exception e) {
      log.error("Exception while parsing google event={}", googleEvent, e);
      throw e;
    }
  }

  private String convertTimeZoneTimeToLocalTimeZone(String timeZoneTime) {
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeZoneTime);
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return zonedDateTime.format(dateTimeFormatter);
  }
}
