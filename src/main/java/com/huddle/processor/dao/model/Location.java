package com.huddle.processor.dao.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Location {
  int id;
  String city;
  String state;
  String address;
  String calendarId;
}
