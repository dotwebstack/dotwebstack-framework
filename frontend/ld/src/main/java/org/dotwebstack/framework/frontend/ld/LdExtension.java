package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdExtension {

  private static final Logger LOG = LoggerFactory.getLogger(LdExtension.class);

  private HttpConfiguration httpConfiguration;

  @Autowired
  public LdExtension(HttpConfiguration httpConfiguration) {
    this.httpConfiguration = Objects.requireNonNull(httpConfiguration);
  }

  @PostConstruct
  public void postLoad() {
    // Dummy statement
    LOG.debug(httpConfiguration.getClass().getName());
  }

}
