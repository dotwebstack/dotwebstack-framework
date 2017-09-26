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

  private ImmutableList<SchemaMapper<? extends Property, ?>> schemaMappers;

  @Autowired
  public SchemaMapperAdapter(@NonNull List<SchemaMapper<? extends Property, ?>> schemaMappers) {
    this.schemaMappers = ImmutableList.copyOf(schemaMappers);
  }

  @SuppressWarnings("unchecked")
  public <S extends Property> Object mapTupleValue(@NonNull S schema, @NonNull Value value) {
    SchemaMapper<? extends Property,
        ?> schemaMapper = schemaMappers.stream().filter(
            candidateMapper -> candidateMapper.supports(schema)).findFirst().orElseThrow(
                () -> new SchemaHandlerRuntimeException(String.format(
                    "No schema handler available for '%s'.", schema.getClass().getName())));

    return ((SchemaMapper<S, ?>) schemaMapper).mapTupleValue(schema, value);
  }

}
