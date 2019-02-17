package com.huddle.processor.dao.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
  Integer id;
  //Todo(Adi): Add to database
  String calendarEventId;
  String type;
  //Todo(Adi): Add to database
   String category;
  //Todo(Adi): Add to database
  String status;
  Integer price;
  String organizer;
  String startTime;
  String endTime;
  String description;
  Integer locationId;
  String created;
  String modified;
}
