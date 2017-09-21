package org.dotwebstack.framework.frontend.ld;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdModule implements HttpModule {

  private LdRequestMapper requestMapper;

  @Autowired
  public LdModule(@NonNull LdRequestMapper requestMapper) {
    this.requestMapper = requestMapper;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    requestMapper.loadRepresentations(httpConfiguration);
  }
}
