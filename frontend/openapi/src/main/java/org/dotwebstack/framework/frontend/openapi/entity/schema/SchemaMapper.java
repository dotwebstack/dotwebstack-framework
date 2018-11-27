package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.v3.oas.models.media.Schema;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;

public interface SchemaMapper<S extends Schema, T> {

  T mapTupleValue(@NonNull S schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext);

  T mapGraphValue(@NonNull S schema, boolean required, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter);

  boolean supports(Schema schema);
}
