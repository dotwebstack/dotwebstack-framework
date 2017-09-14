package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdModule implements HttpModule {

  private LdRequestMapper requestMapper;

  @Autowired
  public LdModule(LdRequestMapper requestMapper) {
    this.requestMapper = Objects.requireNonNull(requestMapper);
  }

  @Override
  public void initialize(HttpConfiguration httpConfiguration) {
    Objects.requireNonNull(httpConfiguration);
    requestMapper.loadRepresentations(httpConfiguration);
  }
}
