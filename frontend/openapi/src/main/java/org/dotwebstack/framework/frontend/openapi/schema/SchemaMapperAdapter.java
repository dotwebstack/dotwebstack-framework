package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.Property;
import java.util.List;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class SchemaMapperAdapter {

  private ImmutableList<SchemaMapper<? extends Property, ?>> schemaHandlers;

  @Autowired
  public SchemaMapperAdapter(@NonNull List<SchemaMapper<? extends Property, ?>> schemaHandlers) {
    this.schemaHandlers = ImmutableList.copyOf(schemaHandlers);
  }

  @SuppressWarnings("unchecked")
  public <S extends Property> Object mapTupleValue(@NonNull S schema, @NonNull Value value) {
    for (SchemaMapper<? extends Property, ?> handler : schemaHandlers) {
      if (handler.supports(schema)) {
        return ((SchemaMapper<S, ?>) handler).mapTupleValue(schema, value);
      }
    }

    throw new SchemaHandlerRuntimeException(
        String.format("No schema handler available for '%s'.", schema.getClass().getName()));
  }

}
