package com.huddle.processor.controllers;

import com.huddle.processor.controllers.response.PendingJob;
import com.huddle.processor.dao.LocationDao;
import com.huddle.processor.dao.PendingJobDao;
import com.huddle.processor.dao.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class PendingJobController {

  @Autowired
  PendingJobDao pendingJobDao;

  @Autowired
  LocationDao locationDao;

  @GetMapping("/pendingJobs")
  public List<PendingJob> getPendingJobs() {
    final List<com.huddle.processor.dao.model.PendingJob> dbPendingJobs = pendingJobDao.getPendingJobs();
    return dbPendingJobs.stream()
        .map(dbPendingJob -> createPendingJob(dbPendingJob))
        .collect(Collectors.toList());
  }

  private PendingJob createPendingJob(com.huddle.processor.dao.model.PendingJob dbPendingJob) {
    com.huddle.processor.controllers.response.Location location = null;
    if (Objects.nonNull(dbPendingJob.getLocationId())) {
      final Location dbLocation = locationDao.getLocation(dbPendingJob.getLocationId());
      if (Objects.nonNull(dbLocation)) {
        location = com.huddle.processor.controllers.response.Location.builder()
            .address(dbLocation.getAddress())
            .calendarId(dbLocation.getCalendarId())
            .city(dbLocation.getCity())
            .state(dbLocation.getState())
            .build();
      }
    }

    return PendingJob.builder()
        .location(location)
        .created(dbPendingJob.getCreated())
        .modified(dbPendingJob.getModified())
        .endDateExl(dbPendingJob.getEndDateExl())
        .startDateInc(dbPendingJob.getStartDateInc())
        .nextDate(dbPendingJob.getNextDate())
        .status(dbPendingJob.getStatus())
        .build();
  }
}
