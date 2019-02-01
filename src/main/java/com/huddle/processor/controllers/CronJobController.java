package com.huddle.processor.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
public class CronJobController {
  final static String IST_TIMEZONE_OFFSET = "05:30";

  @Autowired
  EventController eventController;

  @GetMapping("/cron/process/events")
  public String processEvents() throws IOException {
    ZonedDateTime todayDateTime = ZonedDateTime.now(ZoneId.of(String.format("+%s", IST_TIMEZONE_OFFSET)));
    ZonedDateTime yesterdaysDateTime = todayDateTime.minusDays(1);
    return eventController.processEvents(
        String.format("%sT00:00:00.000", yesterdaysDateTime.toLocalDate()),
        String.format("%sT00:00:00.000", todayDateTime.toLocalDate()),
        IST_TIMEZONE_OFFSET);
  }
}
