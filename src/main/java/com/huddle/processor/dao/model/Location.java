package com.huddle.processor.dao.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Location {
  int id;
  String name;
  String city;
  String state;
  String address;
  String calendarId;
}
