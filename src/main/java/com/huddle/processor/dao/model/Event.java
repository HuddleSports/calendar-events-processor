package com.huddle.processor.dao.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Event {
    Integer id;
    String type;
    Integer price;
    String organizer;
    String startTime;
    String endTime;
    String description;
    Integer locationId;
    String created;
    String modified;
}
