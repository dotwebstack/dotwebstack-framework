package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpExtension;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdExtension implements HttpExtension {

  private static final Logger LOG = LoggerFactory.getLogger(LdExtension.class);

  private HttpConfiguration httpConfiguration;

  private RequestMapper requestMapper;

  @Autowired
  public LdExtension(RepresentationResourceProvider representationResourceProvider) {
    Objects.requireNonNull(representationResourceProvider);

    this.requestMapper = new RequestMapper(representationResourceProvider);
  }

  @Override
  public void initialize(HttpConfiguration httpConfiguration) {
    this.httpConfiguration = Objects.requireNonNull(httpConfiguration);
    requestMapper.loadRepresentations(httpConfiguration);
  }

  @PostConstruct
  public void postLoad() {
    // Dummy statement
  }

}
