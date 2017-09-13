package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdExtension implements HttpExtension {

  private RequestMapper requestMapper;

  @Autowired
  public LdExtension(RequestMapper requestMapper) {
    this.requestMapper = Objects.requireNonNull(requestMapper);
  }

  @Override
  public void initialize(HttpConfiguration httpConfiguration) {
    Objects.requireNonNull(httpConfiguration);
    requestMapper.loadRepresentations(httpConfiguration);
  }
}
