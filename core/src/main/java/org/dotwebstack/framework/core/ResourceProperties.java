package org.dotwebstack.framework.core;

import java.net.URI;

public class ResourceProperties {

  private ResourceProperties() {}

  private static final String CLASSPATH_PREFIX = "classpath:/";

  private static final String FILE_PREFIX = "file:/";

  private static final String CONFIG_PATH = "config/";

  public static String getConfigPath() {
    return CONFIG_PATH;
  }

  public static URI getFileConfigPath() {
    return URI.create(FILE_PREFIX + CONFIG_PATH);
  }

  public static URI getResourcePath() {
    return URI.create(CLASSPATH_PREFIX + CONFIG_PATH);
  }

}
