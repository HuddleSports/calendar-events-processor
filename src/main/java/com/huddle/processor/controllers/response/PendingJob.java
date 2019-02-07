package com.huddle.processor.controllers.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PendingJob {
  String startDateInc;
  String endDateExl;
  String nextDate;
  com.huddle.processor.dao.model.PendingJob.Status status;
  String created;
  String modified;
  Location location;
}
