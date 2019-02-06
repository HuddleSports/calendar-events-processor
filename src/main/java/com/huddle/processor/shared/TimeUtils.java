package com.huddle.processor.shared;

import java.time.ZonedDateTime;

public class TimeUtils {
  public static ZonedDateTime getDateTime(final String dateTime,
                                          final String timeZoneOffset) {
    return ZonedDateTime.parse(String.format("%s+%s", dateTime, timeZoneOffset));
  }
}
