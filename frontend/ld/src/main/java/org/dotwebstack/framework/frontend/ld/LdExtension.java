package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import javax.annotation.PostConstruct;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpExtension;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdExtension implements HttpExtension {

  private RequestMapper requestMapper;

  @Autowired
  public LdExtension(RepresentationResourceProvider representationResourceProvider) {
    Objects.requireNonNull(representationResourceProvider);

    this.requestMapper = new RequestMapper(representationResourceProvider);
  }

  @Override
  public void initialize(HttpConfiguration httpConfiguration) {
    Objects.requireNonNull(httpConfiguration);
    requestMapper.loadRepresentations(httpConfiguration);
  }

  @PostConstruct
  public void postLoad() {
    // Dummy statement
  }

}
