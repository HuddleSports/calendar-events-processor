package com.huddle.processor.google_calendar.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Event {
  String startTime;
  String endTime;
  String description;
  String calendarEventId;
  String calendarEventStatus;
}
