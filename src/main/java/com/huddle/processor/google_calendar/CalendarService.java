package com.huddle.processor.google_calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Channel;
import com.google.api.services.calendar.model.Events;
import com.google.common.net.MediaType;
import com.huddle.processor.google_calendar.response.Calendar;
import com.huddle.processor.google_calendar.response.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.net.MediaType.JSON_UTF_8;

@Component
@Log4j2
public class CalendarService {

  @Autowired
  private com.google.api.services.calendar.Calendar calendarClient;

  @Autowired
  private Credential credential;

  public List<Calendar> getCalendars() throws IOException {
    log.info("Starting fetching of Google Calendars");
    com.google.api.services.calendar.Calendar.CalendarList.List calendarsList = calendarClient.calendarList().list();

    CalendarList calendarList = calendarsList.execute();
    List<CalendarListEntry> items = calendarList.getItems();
    List<com.huddle.processor.google_calendar.response.Calendar> calendars = new ArrayList<>();
    if (!items.isEmpty()) {
      for (CalendarListEntry calendarListEntry : items) {
        calendars.add(com.huddle.processor.google_calendar.response.Calendar
            .builder()
            .description(calendarListEntry.getDescription())
            .id(calendarListEntry.getId())
            .kind(calendarListEntry.getKind())
            .location(calendarListEntry.getLocation())
            .primary(calendarListEntry.getPrimary())
            .summary(calendarListEntry.getSummary())
            .summaryOverride(calendarListEntry.getSummaryOverride())
            .timeZone(calendarListEntry.getTimeZone())
            .build());
      }
    }
    log.info("Fetched googleCalendars={}", calendars.size());
    return calendars;
  }

  public Event getEvent(final String calendarId,
                        final String eventId) throws IOException {
    com.google.api.services.calendar.model.Event event =
        calendarClient.events().get(calendarId, eventId).execute();
    return createEvent(event);
  }

  public void watchEvents(final String startTimeIncl,
                          final String endTimeExl,
                          final String timezoneOffset,
                          final String calendarId) throws IOException {
    final Channel eventsChannel = new Channel();
    //To environment specific
    eventsChannel.setAddress("https://huddle-quick-solutions.appspot.com/handle/notification");
    eventsChannel.setType("web_hook");
    eventsChannel.setExpiration(ZonedDateTime.now().plusDays(40).toInstant().toEpochMilli());
    eventsChannel.setId(UUID.randomUUID().toString());
    com.google.api.services.calendar.Calendar.Events.Watch watchEvents =
        calendarClient.events().watch(calendarId, eventsChannel)
        .setTimeMin(getDateTime(startTimeIncl, timezoneOffset))
        .setTimeMax(getDateTime(endTimeExl, timezoneOffset))
        .setOrderBy("startTime")
        .setSingleEvents(true);
    watchEvents.execute();
  }

  public List<Event> getEvents(final String startTimeIncl,
                               final String endTimeExl,
                               final String timezoneOffset,
                               final String calendarId) throws IOException {
    com.google.api.services.calendar.Calendar.Events.List eventsList = calendarClient.events().list(calendarId)
        .setTimeMin(getDateTime(startTimeIncl, timezoneOffset))
        .setTimeMax(getDateTime(endTimeExl, timezoneOffset))
        .setOrderBy("startTime")
        .setShowDeleted(true)
        .setSingleEvents(true);

    List<com.google.api.services.calendar.model.Event> items = eventsList.execute().getItems();

    return items.stream()
        .map(event -> createEvent(event))
        .collect(Collectors.toList());
  }

  private Event createEvent(com.google.api.services.calendar.model.Event event) {
    DateTime start = event.getStart().getDateTime();
    if (start == null) {
      start = event.getStart().getDate();
    }
    DateTime end = event.getEnd().getDateTime();
    if (end == null) {
      end = event.getEnd().getDate();
    }

    return Event
        .builder()
        .startTime(start.toString())
        .endTime(end.toString())
        .description(event.getSummary())
        .calendarEventId(event.getId())
        .calendarEventStatus(event.getStatus())
        .build();
  }

  private DateTime getDateTime(final String dateTime,
                               final String timeZoneOffset) {
    return new DateTime(String.format("%s+%s", dateTime, timeZoneOffset));
  }
}
