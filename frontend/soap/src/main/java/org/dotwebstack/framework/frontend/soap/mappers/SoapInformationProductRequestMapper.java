package org.dotwebstack.framework.frontend.soap.mappers;

import lombok.NonNull;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SoapInformationProductRequestMapper implements RequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(
      SoapInformationProductRequestMapper.class);

  private final InformationProductResourceProvider informationProductResourceProvider;

  @Autowired
  public SoapInformationProductRequestMapper(
      @NonNull InformationProductResourceProvider informationProductLoader) {
    this.informationProductResourceProvider = informationProductLoader;
  }

  public void map(Resource.Builder resourceBuilder, String absolutePath) {
    LOG.debug("SOAP-Mapper: TODO");
  }

}
