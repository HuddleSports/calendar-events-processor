package com.huddle.processor.google.api.client.extensions.jdo.dao;

import com.google.api.client.util.Preconditions;
import com.huddle.processor.google.api.client.extensions.jdo.dao.model.JdoValue;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateCrud;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JdoValueDao {
  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<JdoValue> jdoValueRowMapper;
  private final JdbcTemplateCrud<JdoValue, String> jdoValueCrud;

  @Autowired
  public JdoValueDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    jdoValueRowMapper =
        JdbcTemplateMapperFactory
            .newInstance()
            .ignorePropertyNotFound()
            .newRowMapper(JdoValue.class);
    jdoValueCrud =
        JdbcTemplateMapperFactory
            .newInstance()
            .ignorePropertyNotFound()
            .crud(JdoValue.class, String.class)
            .to(jdbcTemplate, "JdoValue");
  }

  public List<JdoValue> getJdoValues() {
    String query = "Select * from JdoValue";

    return jdbcTemplate.query(query, jdoValueRowMapper);
  }

  public Optional<JdoValue> getJdoValue(final String key) {
    String query = "Select * " +
        "from JdoValue " +
        "where " +
        "`key` = ? ";

    Object[] params = new Object[]{key};

    List<JdoValue> jdoValues = jdbcTemplate.query(query, params, jdoValueRowMapper);
    Preconditions.checkArgument(jdoValues.size() <= 1);
    return jdoValues.isEmpty() ? Optional.empty() : Optional.of(jdoValues.get(0));
  }

  public void create(final JdoValue jdoValue) {
    String query = "INSERT INTO JdoValue(`key`, `binaryData`) VALUES(?, ?)";
    Object[] params = new Object[]{jdoValue.getKey(), jdoValue.getBinaryData()};
    jdbcTemplate.update(query, params);
  }

  public void delete(final String key) {
    jdoValueCrud.delete(key);
  }
}
