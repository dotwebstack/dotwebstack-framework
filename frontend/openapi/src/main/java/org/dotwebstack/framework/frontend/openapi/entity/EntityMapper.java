package org.dotwebstack.framework.frontend.openapi.entity;

import javax.ws.rs.core.MediaType;

public interface EntityMapper<E extends Entity> {

  Object map(E entity, MediaType mediaType);

}
