package com.huddle.processor.controllers.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Event {
  String type;
  int price;
  String organizer;
  String startTime;
  String endTime;
}
