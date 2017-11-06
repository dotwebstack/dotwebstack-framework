package org.dotwebstack.framework.frontend.ld;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.dotwebstack.framework.frontend.ld.mappers.LdRedirectionRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.LdRepresentationRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdModule implements HttpModule {

  private LdRepresentationRequestMapper ldRepresentationRequestMapper;

  private LdRedirectionRequestMapper ldRedirectionRequestMapper;

  @Autowired
  public LdModule(@NonNull LdRepresentationRequestMapper ldRepresentationRequestMapper,
      @NonNull LdRedirectionRequestMapper ldRedirectionRequestMapper) {
    this.ldRepresentationRequestMapper = ldRepresentationRequestMapper;
    this.ldRedirectionRequestMapper = ldRedirectionRequestMapper;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);
    ldRedirectionRequestMapper.loadRedirections(httpConfiguration);
  }
}
