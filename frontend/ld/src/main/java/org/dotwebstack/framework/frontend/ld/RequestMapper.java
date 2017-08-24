package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class RequestMapper implements ResourceLoaderAware {

  private ResourceLoader resourceLoader;

  private HttpConfiguration httpConfiguration;

  private RepresentationResourceProvider representationResourceProvider;

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = Objects.requireNonNull(resourceLoader);
  }
}
