package org.dotwebstack.framework.frontend.ld;

import javax.annotation.PostConstruct;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class LdExtension {

  private static final Logger LOG = LoggerFactory.getLogger(LdExtension.class);

  private HttpConfiguration httpConfiguration;

  @Autowired
  public LdExtension(HttpConfiguration httpConfiguration) {
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
