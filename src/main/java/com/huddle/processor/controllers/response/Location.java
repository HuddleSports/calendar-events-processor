package com.huddle.processor.controllers.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Location {
  String city;
  String state;
  String address;
  String calendarId;
}
