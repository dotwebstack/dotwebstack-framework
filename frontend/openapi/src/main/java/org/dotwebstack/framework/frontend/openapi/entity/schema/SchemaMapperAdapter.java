package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaMapperAdapter {

  private final ImmutableList<SchemaMapper<? extends Schema, ?>> schemaMappers;

  @Autowired
  public SchemaMapperAdapter(@NonNull List<SchemaMapper<? extends Schema, ?>> schemaMappers) {
    this.schemaMappers = ImmutableList.copyOf(schemaMappers);
  }

  public <S extends Schema> Object mapTupleValue(@NonNull S schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return getSchemaMapper(schema).mapTupleValue(schema, entity, valueContext);
  }

  public <S extends Schema> Object mapGraphValue(@NonNull S schema, boolean required,
      GraphEntity entity, @NonNull ValueContext valueContext,
      @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    return getSchemaMapper(schema)
        .mapGraphValue(schema, required, entity, valueContext, schemaMapperAdapter);
  }

  @SuppressWarnings("unchecked")
  private <S extends Schema> SchemaMapper<S, ?> getSchemaMapper(@NonNull S schema) {
    return (SchemaMapper<S, ?>) schemaMappers.stream() //
        .filter(candidateMapper -> candidateMapper.supports(schema)) //
        .findFirst() //
        .orElseThrow(() -> new SchemaMapperRuntimeException(//
            String.format("No schema mapper available for '%s'.", schema.getClass().getName())));
  }
}
