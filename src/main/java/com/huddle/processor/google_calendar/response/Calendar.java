package com.huddle.processor.google_calendar.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Calendar {
  String description;
  String id;
  String kind;
  String location;
  Boolean primary;
  String summary;
  String summaryOverride;
  String timeZone;
}
