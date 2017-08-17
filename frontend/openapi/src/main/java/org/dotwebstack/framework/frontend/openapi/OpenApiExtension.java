package org.dotwebstack.framework.frontend.openapi;

import javax.annotation.PostConstruct;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class OpenApiExtension {

  private static final Logger LOG = LoggerFactory.getLogger(OpenApiExtension.class);

  private HttpConfiguration httpConfiguration;

  @Autowired
  public OpenApiExtension(HttpConfiguration httpConfiguration) {
    this.httpConfiguration = httpConfiguration;
  }

  @PostConstruct
  public void postLoad() {
    // Dummy statement
    if (LOG.isDebugEnabled()) {
      LOG.debug(httpConfiguration.toString());
    }
  }

}
