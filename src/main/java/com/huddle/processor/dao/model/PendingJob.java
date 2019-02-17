package com.huddle.processor.dao.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingJob {
  int id;
  String startDateInc;
  String endDateExl;
  String nextDate;
  Status status;
  String created;
  String modified;
  Integer locationId;
  //Todo(Adi): Add to database
  Boolean registerCallback;

  public enum Status {
    PENDING,
    COMPLETED,
    CANCELED
  }
}
