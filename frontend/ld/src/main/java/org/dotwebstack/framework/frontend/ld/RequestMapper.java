package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

public class RequestMapper implements ResourceLoaderAware {

  private static final Logger LOG = LoggerFactory.getLogger(RequestMapper.class);

  private ResourceLoader resourceLoader;

  private HttpConfiguration httpConfiguration;

  private RepresentationResourceProvider representationResourceProvider;

  @Autowired
  public RequestMapper(RepresentationResourceProvider representationResourceProvider,
      HttpConfiguration httpConfiguration) {
    this.representationResourceProvider = Objects.requireNonNull(representationResourceProvider);
    this.httpConfiguration = Objects.requireNonNull(httpConfiguration);
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = Objects.requireNonNull(resourceLoader);
  }

  public void loadRepresenations() {
    for (Representation representation : representationResourceProvider.getAll().values()) {

    }
  }

  private void mapRepresentation(Representation representation) {

  }

  private String createBasePath(Representation representation) {
    return "";
  }
}
