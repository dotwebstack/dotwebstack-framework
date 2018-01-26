package org.dotwebstack.framework.frontend.ld;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.FormatPreMatchingRequestFilter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.dotwebstack.framework.frontend.ld.mappers.LdRedirectionRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.LdRepresentationRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdModule implements HttpModule {

  private final LdRepresentationRequestMapper ldRepresentationRequestMapper;

  private final LdRedirectionRequestMapper ldRedirectionRequestMapper;

  private final SupportedMediaTypesScanner supportedMediaTypesScanner;

  @Autowired
  public LdModule(@NonNull LdRepresentationRequestMapper ldRepresentationRequestMapper,
      @NonNull LdRedirectionRequestMapper ldRedirectionRequestMapper,
      @NonNull SupportedMediaTypesScanner supportedMediaTypesScanner) {
    this.ldRepresentationRequestMapper = ldRepresentationRequestMapper;
    this.ldRedirectionRequestMapper = ldRedirectionRequestMapper;
    this.supportedMediaTypesScanner = supportedMediaTypesScanner;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    httpConfiguration.register(FormatPreMatchingRequestFilter.class);
    ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);
    ldRedirectionRequestMapper.loadRedirections(httpConfiguration);

    supportedMediaTypesScanner.getGraphEntityWriters().forEach(httpConfiguration::register);
    supportedMediaTypesScanner.getTupleEntityWriters().forEach(httpConfiguration::register);
  }

}
