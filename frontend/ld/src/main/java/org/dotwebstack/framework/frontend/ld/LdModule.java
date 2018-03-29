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

  private final SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Autowired
  public LdModule(@NonNull LdEndPointRequestMapper ldEndPointRequestMapper,
      @NonNull LdRedirectionRequestMapper ldRedirectionRequestMapper,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner) {
    this.ldEndPointRequestMapper = ldEndPointRequestMapper;
    this.ldRedirectionRequestMapper = ldRedirectionRequestMapper;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    httpConfiguration.register(FormatPreMatchingRequestFilter.class);
    ldEndPointRequestMapper.loadEndPoints(httpConfiguration);
    ldRedirectionRequestMapper.loadRedirections(httpConfiguration);

    supportedWriterMediaTypesScanner.getGraphEntityWriters().forEach(httpConfiguration::register);
    supportedWriterMediaTypesScanner.getTupleEntityWriters().forEach(httpConfiguration::register);

    supportedReaderMediaTypesScanner.getModelReaders().forEach(httpConfiguration::register);
  }

}
