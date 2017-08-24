package org.dotwebstack.framework.frontend.ld;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class RequestMapper implements ResourceLoaderAware {

  private ResourceLoader resourceLoader;

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {

  }
}
