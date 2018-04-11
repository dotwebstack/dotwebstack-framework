package org.dotwebstack.framework.frontend.ld;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.FormatPreMatchingRequestFilter;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.dotwebstack.framework.frontend.ld.mappers.DirectEndPointRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.DynamicEndPointRequestMapper;
import org.dotwebstack.framework.frontend.ld.mappers.LdRedirectionRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LdModule implements HttpModule {

  private final DirectEndPointRequestMapper directEndPointRequestMapper;

  private final DynamicEndPointRequestMapper dynamicEndPointRequestMapper;

  private final LdRedirectionRequestMapper ldRedirectionRequestMapper;

  private final SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  private final SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Autowired
  public LdModule(@NonNull DynamicEndPointRequestMapper dynamicEndPointRequestMapper,
      @NonNull DirectEndPointRequestMapper directEndPointRequestMapper,
      @NonNull LdRedirectionRequestMapper ldRedirectionRequestMapper,
      @NonNull SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner,
      @NonNull SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner) {
    this.dynamicEndPointRequestMapper = dynamicEndPointRequestMapper;
    this.directEndPointRequestMapper = directEndPointRequestMapper;
    this.ldRedirectionRequestMapper = ldRedirectionRequestMapper;
    this.supportedWriterMediaTypesScanner = supportedWriterMediaTypesScanner;
    this.supportedReaderMediaTypesScanner = supportedReaderMediaTypesScanner;
  }

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    httpConfiguration.register(FormatPreMatchingRequestFilter.class);
    dynamicEndPointRequestMapper.loadDynamicEndPoints(httpConfiguration);
    directEndPointRequestMapper.loadDirectEndPoints(httpConfiguration);
    ldRedirectionRequestMapper.loadRedirections(httpConfiguration);

    supportedWriterMediaTypesScanner.getGraphEntityWriters().forEach(httpConfiguration::register);
    supportedWriterMediaTypesScanner.getTupleEntityWriters().forEach(httpConfiguration::register);

    supportedReaderMediaTypesScanner.getModelReaders().forEach(httpConfiguration::register);
  }

}
