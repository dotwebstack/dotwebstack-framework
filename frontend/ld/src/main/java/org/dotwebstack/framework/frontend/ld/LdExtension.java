package org.dotwebstack.framework.frontend.ld;

import org.dotwebstack.framework.PostLoadExtension;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class LdExtension implements PostLoadExtension {

  private static final Logger LOG = LoggerFactory.getLogger(LdExtension.class);

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
