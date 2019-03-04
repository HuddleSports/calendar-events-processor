package com.huddle.processor.dao.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
  Integer id;
  String calendarEventId;
  String type;
  String category;
  String status;
  String calendarEventStatus;
  Integer price;
  String organizer;
  String startTime;
  String endTime;
  String description;
  Integer locationId;
  String created;
  String modified;
}
