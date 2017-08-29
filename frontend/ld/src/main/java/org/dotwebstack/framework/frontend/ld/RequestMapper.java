package org.dotwebstack.framework.frontend.ld;

import java.util.Objects;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.handlers.GetRequestHandler;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(RequestMapper.class);

  private HttpConfiguration httpConfiguration;

  private RepresentationResourceProvider representationResourceProvider;

  @Autowired
  public RequestMapper(RepresentationResourceProvider representationResourceProvider,
      HttpConfiguration httpConfiguration) {
    this.representationResourceProvider = Objects.requireNonNull(representationResourceProvider);
    this.httpConfiguration = Objects.requireNonNull(httpConfiguration);
  }

  public void loadRepresenations() {
    for (Representation representation : representationResourceProvider.getAll().values()) {
      if (representation.getStage() != null) {
        mapRepresentation(representation);
      } else {
        LOG.warn("Representation '{}' is not mapped to a stage.", representation.getIdentifier());
      }
    }
  }

  private void mapRepresentation(Representation representation) {

  }

  private String createBasePath(Representation representation) {
    return "";
  }
}
