package com.huddle.processor.google.api.client.extensions.jdo.dao.model;

import com.google.api.client.util.IOUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;

@Getter
@Setter
public class JdoValue {
  String key;
  byte[] binaryData;

  public JdoValue() {
  }

  public <V extends Serializable> JdoValue(String key, V data) throws IOException {
    this.key = key;
    serialize(data);
  }

  public <V extends Serializable> void serialize(V data) throws IOException {
    binaryData = IOUtils.serialize(data);
  }

  public <V extends Serializable> V deserialize() throws IOException {
    return IOUtils.deserialize(binaryData);
  }
}
