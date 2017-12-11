package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ResponseProperty;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperContext;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class TupleEntityMapper implements EntityMapper<TupleEntity> {

  private static final Logger LOG = LoggerFactory.getLogger(TupleEntityMapper.class);

  private final SchemaMapperAdapter schemaMapperAdapter;

  @Autowired
  public TupleEntityMapper(@NonNull SchemaMapperAdapter schemaMapperAdapter) {
    this.schemaMapperAdapter = schemaMapperAdapter;
  }

  @Override
  public Object map(@NonNull TupleEntity entity, @NonNull MediaType mediaType) {
    Property schema = entity.getSchemaMap().get(mediaType);

    SchemaMapperContext schemaMapperContext = SchemaMapperContextImpl.builder().build();

    if (schema == null) {
      throw new EntityMapperRuntimeException(
          String.format("No schema found for media type '%s'.", mediaType.toString()));
    }

    if (schema instanceof ResponseProperty) {
      schema = ((ResponseProperty) schema).getSchema();
    }
    if (schema instanceof ObjectProperty) {
      return mapObject(entity, (ObjectProperty) schema, schemaMapperContext);
    }
    if (schema instanceof ArrayProperty) {
      return mapCollection(entity, (ArrayProperty) schema, schemaMapperContext);
    }

    return ImmutableMap.of();
  }

  private Object mapObject(TupleEntity entity, ObjectProperty schema,
      SchemaMapperContext schemaMapperContext) {
    TupleQueryResult result = entity.getResult();
    if (result.hasNext()) {
      BindingSet bindingSet = result.next();
      if (result.hasNext()) {
        LOG.warn("TupleQueryResult yielded several bindingsets. Only parsing the first.");
      }

      return mapBindingSet(bindingSet, schema.getProperties(), schemaMapperContext);
    } else {
      throw new EntityMapperRuntimeException("TupleQueryResult did not yield any values.");
    }
  }

  private Object mapCollection(TupleEntity entity, ArrayProperty schema,
      SchemaMapperContext schemaMapperContext) {
    Property itemSchema = schema.getItems();

    if (itemSchema == null) {
      throw new EntityMapperRuntimeException("Array schemas must have an 'items' property.");
    }

    if (!(itemSchema instanceof ObjectProperty)) {
      throw new EntityMapperRuntimeException(
          "Only array items of type 'object' are supported for now.");
    }

    TupleQueryResult result = entity.getResult();

    ImmutableList.Builder<Map<String, Object>> collectionBuilder = new ImmutableList.Builder<>();
    Map<String, Property> itemProperties = ((ObjectProperty) itemSchema).getProperties();

    while (result.hasNext()) {
      collectionBuilder.add(mapBindingSet(result.next(), itemProperties, schemaMapperContext));
    }

    return collectionBuilder.build();
  }

  private ImmutableMap<String, Object> mapBindingSet(BindingSet bindingSet,
      Map<String, Property> itemProperties, SchemaMapperContext schemaMapperContext) {
    ImmutableMap.Builder<String, Object> itemBuilder = new ImmutableMap.Builder<>();

    itemProperties.forEach((name, property) -> {
      if (property.getRequired() && !bindingSet.hasBinding(name)) {
        throw new EntityMapperRuntimeException(String.format("Property '%s' is required.", name));
      }

      if (!bindingSet.hasBinding(name)) {
        itemBuilder.put(name, Optional.absent());
        return;
      }

      schemaMapperContext.setValue(bindingSet.getValue(name));
      itemBuilder.put(name, schemaMapperAdapter.mapTupleValue(property, schemaMapperContext));
    });

    return itemBuilder.build();
  }

}
