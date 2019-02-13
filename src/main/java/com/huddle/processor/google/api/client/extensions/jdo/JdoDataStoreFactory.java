package com.huddle.processor.google.api.client.extensions.jdo;

import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.huddle.processor.google.api.client.extensions.jdo.dao.JdoValueDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class JdoDataStoreFactory extends AbstractDataStoreFactory {

  @Autowired
  JdoValueDao jdoValueDao;

  @Override
  protected <V extends Serializable> DataStore<V> createDataStore(String id) {
    return new JdoDataStore<>(this, id, jdoValueDao);
  }
}
