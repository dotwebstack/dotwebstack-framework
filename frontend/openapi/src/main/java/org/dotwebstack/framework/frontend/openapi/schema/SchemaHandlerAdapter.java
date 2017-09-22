package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.Property;
import java.util.List;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class SchemaHandlerAdapter {

  private ImmutableList<SchemaHandler<? extends Property, ?>> schemaHandlers;

  @Autowired
  public SchemaHandlerAdapter(@NonNull List<SchemaHandler<? extends Property, ?>> schemaHandlers) {
    this.schemaHandlers = ImmutableList.copyOf(schemaHandlers);
  }

  @SuppressWarnings("unchecked")
  public <S extends Property> Object handleTupleValue(@NonNull S schema, @NonNull Value value) {
    for (SchemaHandler<? extends Property, ?> handler : schemaHandlers) {
      if (handler.supports(schema)) {
        return ((SchemaHandler<S, ?>) handler).handleTupleValue(schema, value);
      }
    }

    throw new SchemaHandlerRuntimeException(
        String.format("No schema handler available for '%s'.", schema.getClass().getName()));
  }

}
