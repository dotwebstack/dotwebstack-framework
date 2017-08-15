package org.dotwebstack.framework.frontend.ld;

import org.dotwebstack.framework.Extension;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class LdExtension implements Extension {

  private static final Logger LOG = LoggerFactory.getLogger(BackendLoader.class);

  private HttpConfiguration httpConfiguration;

  @Autowired
  public LdExtension(HttpConfiguration httpConfiguration) {
    this.httpConfiguration = httpConfiguration;
  }

  @Override
  public void postLoad() {
    // Dummy statement
    if (LOG.isDebugEnabled()) {
      LOG.debug(httpConfiguration.toString());
    }
  }

}
