package com.huddle.processor.dao.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Location {
  int id;
  //Todo(Adi): Add to database
  String name;
  String city;
  String state;
  String address;
  //Make primary key
  String calendarId;
}
