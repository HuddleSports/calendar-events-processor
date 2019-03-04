package com.huddle.processor.controllers;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@RestController
@Log4j2
public class EventNotificationController {

  @Autowired
  EventController eventController;

  @PostMapping("handle/notification")
  public String handleNotification(@RequestHeader(name = "X-Goog-Channel-ID") String channelId,
                                   @RequestHeader(name = "X-Goog-Resource-ID") String resourceId,
                                   @RequestHeader(name = "X-Goog-Resource-URI") String resourceURI,
                                   @RequestHeader(name = "X-Goog-Resource-State") String state) throws IOException, URISyntaxException {
    log.info("Received notification with channelId={}, resourceId={}, resourceURI={}, state={}",
        channelId, resourceId, resourceURI, state);
    if (!state.equalsIgnoreCase("exists")) {
      return "Ignoring Notification as not in exists state";
    }
    List<String> parts = Arrays.asList(resourceURI.split("/"));
    String calendarId = parts.get(parts.size() - 2);
    eventController.processEvents(calendarId, resourceURI);
    return "Successfully Handled Notification";
  }

}
