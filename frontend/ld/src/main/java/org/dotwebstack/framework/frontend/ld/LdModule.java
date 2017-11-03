package org.dotwebstack.framework.frontend.ld;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdModule implements HttpModule {

  private final LdRequestMapper requestMapper;

  private final SupportedMediaTypesScanner supportedMediaTypesScanner;

  @Autowired
  public LdModule(@NonNull LdRequestMapper requestMapper,
      @NonNull SupportedMediaTypesScanner supportedMediaTypesScanner) {
    this.requestMapper = requestMapper;
    this.supportedMediaTypesScanner = supportedMediaTypesScanner;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    requestMapper.loadRepresentations(httpConfiguration);

    supportedMediaTypesScanner.getGraphEntityWriters().forEach(httpConfiguration::register);
    supportedMediaTypesScanner.getTupleEntityWriters().forEach(httpConfiguration::register);
  }
}
