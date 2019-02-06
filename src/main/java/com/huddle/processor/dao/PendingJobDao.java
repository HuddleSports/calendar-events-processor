package com.huddle.processor.dao;

import com.huddle.processor.dao.model.Location;
import com.huddle.processor.dao.model.PendingJob;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateCrud;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.huddle.processor.dao.model.PendingJob.Status.PENDING;

@Component
public class PendingJobDao {

  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<PendingJob> pendingJobRowMapper;
  private final JdbcTemplateCrud<PendingJob, Long> pendingJobCrud;

  @Autowired
  public PendingJobDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    pendingJobRowMapper =
        JdbcTemplateMapperFactory
            .newInstance()
            .ignorePropertyNotFound()
            .newRowMapper(PendingJob.class);
    pendingJobCrud =
        JdbcTemplateMapperFactory
            .newInstance()
            .ignorePropertyNotFound()
            .crud(PendingJob.class, Long.class)
            .to(jdbcTemplate, "PendingJob");
  }

  public List<PendingJob> getPendingJobs() {
    String query = "Select * " +
        "from PendingJob " +
        "where " +
        "status = ? ";

    Object[] params = new Object[]{PENDING.toString()};

    return jdbcTemplate.query(query, params, pendingJobRowMapper);
  }

  public void updatePendingJobs(final List<PendingJob> pendingJobs) {
    if (CollectionUtils.isEmpty(pendingJobs)) {
      return;
    }
    pendingJobCrud.update(pendingJobs);
  }
}
