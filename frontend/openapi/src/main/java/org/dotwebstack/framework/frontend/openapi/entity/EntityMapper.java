package org.dotwebstack.framework.frontend.openapi.entity;

import javax.ws.rs.core.MediaType;
import lombok.NonNull;

public interface EntityMapper<E extends Entity<?>> {

  Object mapGraph(@NonNull GraphEntity entity, @NonNull MediaType mediaType, @NonNull GraphEntityContext graphEntityContext);

  Object mapTuple(E entity, MediaType mediaType);

}
