package org.dotwebstack.framework.frontend.ld;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.FormatPreMatchingRequestFilter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.dotwebstack.framework.frontend.ld.mappers.LdEndPointRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.LdRedirectionRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdModule implements HttpModule {

  private final LdEndPointRequestMapper ldEndPointRequestMapper;

  private final LdRedirectionRequestMapper ldRedirectionRequestMapper;

  private final SupportedMediaTypesScanner supportedMediaTypesScanner;

  @Autowired
  public LdModule(@NonNull LdEndPointRequestMapper ldEndPointRequestMapper,
      @NonNull LdRedirectionRequestMapper ldRedirectionRequestMapper,
      @NonNull SupportedMediaTypesScanner supportedMediaTypesScanner) {
    this.ldEndPointRequestMapper = ldEndPointRequestMapper;
    this.ldRedirectionRequestMapper = ldRedirectionRequestMapper;
    this.supportedMediaTypesScanner = supportedMediaTypesScanner;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    httpConfiguration.register(FormatPreMatchingRequestFilter.class);
    ldEndPointRequestMapper.loadEndPoints(httpConfiguration);
    ldRedirectionRequestMapper.loadRedirections(httpConfiguration);

    supportedMediaTypesScanner.getGraphEntityWriters().forEach(httpConfiguration::register);
    supportedMediaTypesScanner.getTupleEntityWriters().forEach(httpConfiguration::register);
  }

}
