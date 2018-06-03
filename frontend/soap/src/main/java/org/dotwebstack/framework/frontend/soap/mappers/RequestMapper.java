package org.dotwebstack.framework.frontend.soap.mappers;

import org.glassfish.jersey.server.model.Resource;

public interface RequestMapper {

  void map(Resource.Builder resourceBuilder, String absolutePath);

}
