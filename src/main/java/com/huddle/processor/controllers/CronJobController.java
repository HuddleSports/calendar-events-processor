package com.huddle.processor.controllers;

import com.huddle.processor.dao.PendingJobDao;
import com.huddle.processor.dao.model.PendingJob;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.huddle.processor.dao.model.PendingJob.Status.COMPLETED;
import static com.huddle.processor.dao.model.PendingJob.Status.PENDING;
import static com.huddle.processor.shared.Constants.IST_TIMEZONE_OFFSET;

@RestController
@Log4j2
public class CronJobController {
  @Autowired
  EventController eventController;

  @Autowired
  PendingJobDao pendingJobDao;

  @GetMapping("/cron/process/pendingJobs")
  public String processPendingJobs() throws IOException {
    List<PendingJob> pendingJobs = pendingJobDao.getPendingJobs();
    if (CollectionUtils.isEmpty(pendingJobs)) {
      return "No Pending Jobs to Process!";
    }

    //Todo(Adi): do in parallel
    for (PendingJob pendingJob : pendingJobs) {
      processPendingJob(pendingJob);
    }
    pendingJobDao.updatePendingJobs(pendingJobs);
    return "Processed 1 date of all Pending Jobs";

  }

  private void processPendingJob(final PendingJob pendingJob) throws IOException {
    LocalDate startDateInc = LocalDate.parse(pendingJob.getNextDate());
    LocalDate endDateExl = startDateInc.plusDays(1);
    ZonedDateTime todayDateTime = ZonedDateTime.now(ZoneId.of(String.format("+%s", IST_TIMEZONE_OFFSET)));
    //validation
    if(todayDateTime.toLocalDate().isEqual(startDateInc)) {
      log.info("Processing startDateInc={} has not finished yet. Waiting for it to finish", startDateInc);
      return;
    }
    eventController.processEvents(
        String.format("%sT00:00:00.000", startDateInc),
        String.format("%sT00:00:00.000", endDateExl),
        IST_TIMEZONE_OFFSET);

    PendingJob.Status updatedStatus = pendingJob.getStatus();
    if (pendingJob.getEndDateExl() != null) {
      LocalDate pendingJobEndDateExl = LocalDate.parse(pendingJob.getEndDateExl());
      if (pendingJobEndDateExl.isAfter(endDateExl)) {
        updatedStatus = PENDING;
      } else {
        updatedStatus = COMPLETED;
      }
    }
    pendingJob.setStatus(updatedStatus);
    pendingJob.setNextDate(endDateExl.toString());
  }
}
