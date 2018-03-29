package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.Property;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaMapperAdapter {

  private ImmutableList<SchemaMapper<? extends Property, ?>> schemaMappers;

  @Autowired
  public SchemaMapperAdapter(@NonNull List<SchemaMapper<? extends Property, ?>> schemaMappers) {
    this.schemaMappers = ImmutableList.copyOf(schemaMappers);
  }

  public <S extends Property> Object mapTupleValue(@NonNull S schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    SchemaMapper<? extends Property, ?> schemaMapper = schemaMappers.stream().filter(
        candidateMapper -> candidateMapper.supports(schema)).findFirst().orElseThrow(
            () -> new SchemaMapperRuntimeException(String.format(
                "No schema mapper available for '%s'.", schema.getClass().getName())));

    return ((SchemaMapper<S, ?>) schemaMapper).mapTupleValue(schema, entity, valueContext);
  }

  @SuppressWarnings("unchecked")
  public <S extends Property> Object mapGraphValue(@NonNull S schema, GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    SchemaMapper<? extends Property, ?> schemaMapper = schemaMappers.stream().filter(
        candidateMapper -> candidateMapper.supports(schema)).findFirst().orElseThrow(
            () -> new SchemaMapperRuntimeException(String.format(
                "No schema mapper available for '%s'.", schema.getClass().getName())));

    return ((SchemaMapper<S, ?>) schemaMapper).mapGraphValue(schema, entity, valueContext,
        schemaMapperAdapter);
  }
}
