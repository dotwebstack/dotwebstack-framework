package org.dotwebstack.framework.frontend.openapi.entity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.schema.SchemaMapperAdapter;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext;
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
    // Already prepared for OASv3 multiple media type support
    Schema schema = entity.getResponse().getContent().get(mediaType).getSchema();

    if (schema == null) {
      throw new EntityMapperRuntimeException(
          String.format("No schema found for media type '%s'.", mediaType.toString()));
    }

    ValueContext valueContext = ValueContext.builder().build();

    if (schema instanceof ObjectSchema) {
      return mapObject(entity, (ObjectSchema) schema, valueContext);
    }

    if (schema instanceof ArraySchema) {
      return mapCollection(entity, (ArraySchema) schema, valueContext);
    }

    return ImmutableMap.of();
  }

  private Object mapObject(TupleEntity entity, ObjectSchema schema, ValueContext valueContext) {
    TupleQueryResult result = entity.getResult();
    if (result.hasNext()) {
      BindingSet bindingSet = result.next();
      if (result.hasNext()) {
        LOG.warn("TupleQueryResult yielded several bindingsets. Only parsing the first.");
      }

      return mapBindingSet(bindingSet, schema.getProperties(), entity, valueContext);
    } else {
      throw new EntityMapperRuntimeException("TupleQueryResult did not yield any values.");
    }
  }

  private Object mapCollection(TupleEntity entity, ArraySchema schema,
      ValueContext valueContext) {
    Schema itemSchema = schema.getItems();

    if (itemSchema == null) {
      throw new EntityMapperRuntimeException("Array schemas must have an 'items' property.");
    }

    if (!(itemSchema instanceof ObjectSchema)) {
      throw new EntityMapperRuntimeException(
          "Only array items of type 'object' are supported for now.");
    }

    TupleQueryResult result = entity.getResult();

    ImmutableList.Builder<Map<String, Object>> collectionBuilder = new ImmutableList.Builder<>();
    Map<String, Schema> itemProperties = ((ObjectSchema) itemSchema).getProperties();

    while (result.hasNext()) {
      collectionBuilder.add(mapBindingSet(result.next(), itemProperties, entity, valueContext));
    }

    return collectionBuilder.build();
  }

  private ImmutableMap<String, Object> mapBindingSet(BindingSet bindingSet,
      Map<String, Schema> itemProperties, TupleEntity entity, ValueContext valueContext) {
    ImmutableMap.Builder<String, Object> itemBuilder = new ImmutableMap.Builder<>();

    itemProperties.forEach((name, property) -> {
      if (property.getRequired().contains(name) && !bindingSet.hasBinding(name)) {
        throw new EntityMapperRuntimeException(String.format("Property '%s' is required.", name));
      }

      if (!bindingSet.hasBinding(name)) {
        itemBuilder.put(name, Optional.absent());
        return;
      }

      ValueContext newValueContext =
          valueContext.toBuilder().value(bindingSet.getValue(name)).build();

      itemBuilder.put(name, schemaMapperAdapter.mapTupleValue(property, entity, newValueContext));
    });

    return itemBuilder.build();
  }

}
