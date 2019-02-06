package com.huddle.processor.google_calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import com.huddle.processor.google_calendar.response.Calendar;
import com.huddle.processor.google_calendar.response.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class CalendarService {

  private final com.google.api.services.calendar.Calendar calendarClient;

  public CalendarService() throws GeneralSecurityException, IOException {
    calendarClient = CalendarClientProvider.get();
  }

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

  public List<Event> getEvents(final String startTimeIncl,
                               final String endTimeExl,
                               final String timezoneOffset,
                               final String calendarId) throws IOException {
    com.google.api.services.calendar.Calendar.Events.List eventsList = calendarClient.events().list(calendarId)
        .setTimeMin(getDateTime(startTimeIncl, timezoneOffset))
        .setTimeMax(getDateTime(endTimeExl, timezoneOffset))
        .setOrderBy("startTime")
        .setSingleEvents(true);

    Events events = eventsList.execute();
    List<com.google.api.services.calendar.model.Event> items = events.getItems();
    List<Event> calendarEvents = new ArrayList<>();
    if (!items.isEmpty()) {
      for (com.google.api.services.calendar.model.Event event : items) {
        DateTime start = event.getStart().getDateTime();
        if (start == null) {
          start = event.getStart().getDate();
        }
        DateTime end = event.getEnd().getDateTime();
        if (end == null) {
          end = event.getEnd().getDate();
        }

        calendarEvents.add(Event
            .builder()
            .startTime(start.toString())
            .endTime(end.toString())
            .description(event.getSummary())
            .build());
      }
    }
    return calendarEvents;
  }

  private DateTime getDateTime(final String dateTime,
                               final String timeZoneOffset) {
    return new DateTime(String.format("%s+%s", dateTime, timeZoneOffset));
  }
}
