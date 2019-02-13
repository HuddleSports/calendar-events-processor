package com.huddle.processor.google.api.client.extensions.jdo;

import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.huddle.processor.google.api.client.extensions.jdo.dao.JdoValueDao;
import com.huddle.processor.google.api.client.extensions.jdo.dao.model.JdoValue;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class JdoDataStore<V extends Serializable> extends AbstractDataStore<V> {

  private final JdoValueDao jdoValueDao;

  /**
   * @param dataStoreFactory data store factory
   * @param id               data store ID
   */
  protected JdoDataStore(DataStoreFactory dataStoreFactory, String id, JdoValueDao jdoValueDao) {
    super(dataStoreFactory, id);
    this.jdoValueDao = jdoValueDao;
  }

  @Override
  public Set<String> keySet() {
    final List<JdoValue> jdoValues = jdoValueDao.getJdoValues();
    return jdoValues.stream()
        .map(jdoValue -> jdoValue.getKey())
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<V> values() {
    final List<JdoValue> jdoValues = jdoValueDao.getJdoValues();
    return jdoValues.stream()
        .map(jdoValue -> {
          try {
            return jdoValue.<V>deserialize();
          } catch (IOException e) {
            log.error("Error fetching JDO Values", e);
            return null;
          }
        })
        .filter(data -> data != null)
        .collect(Collectors.toSet());
  }

  @Override
  public V get(String key) throws IOException {
    Optional<JdoValue> jdoValue = jdoValueDao.getJdoValue(key);
    if (jdoValue.isPresent()) {
      return jdoValue.get().deserialize();
    } else {
      return null;
    }
  }

  @Override
  public JdoDataStore<V> set(String key, V value) throws IOException {
    Optional<JdoValue> jdoValue = jdoValueDao.getJdoValue(key);
    if (!jdoValue.isPresent()) {
      JdoValue newJdoValue = new JdoValue(key, value);
      jdoValueDao.create(newJdoValue);
    }
    return this;
  }

  @Override
  public JdoDataStore<V> clear() throws IOException {
    Set<String> keySet = keySet();
    if (!CollectionUtils.isEmpty(keySet)) {
      keySet.stream()
          .forEach(key -> delete(key));
    }
    return this;
  }

  @Override
  public JdoDataStore<V> delete(String key) {
    if (Objects.nonNull(key)) {
      jdoValueDao.delete(key);
    }
    return this;
  }
}
