package org.dotwebstack.framework.frontend.openapi.entity.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.EntityMapper;
import org.dotwebstack.framework.frontend.openapi.entity.EntityMapperRuntimeException;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.schema.SchemaMapperAdapter;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class GraphEntityMapper implements EntityMapper<GraphEntity> {

  private static final Logger LOG = LoggerFactory.getLogger(GraphEntityMapper.class);

  private SchemaMapperAdapter schemaMapperAdapter;

  @Autowired
  public GraphEntityMapper(@NonNull SchemaMapperAdapter schemaMapperAdapter) {
    this.schemaMapperAdapter = schemaMapperAdapter;
  }

  @Override
  public Object map(@NonNull GraphEntity entity, @NonNull MediaType mediaType) {
    Property schema = entity.getSchemaMap().get(mediaType);

    if (schema == null) {
      throw new EntityMapperRuntimeException(
          String.format("No schema found for media type '%s'.", mediaType.toString()));
    }

    if (schema instanceof ObjectProperty) {
      return mapObject(entity, (ObjectProperty) schema);
    }
    if (schema instanceof ArrayProperty) {
      return mapCollection(entity, (ArrayProperty) schema);
    }

    return ImmutableMap.of();
  }

  private Object mapObject(GraphEntity entity, ObjectProperty schema) {
    GraphQueryResult result = entity.getResult();
    if (result.hasNext()) {
      Statement statement = result.next();
      if (result.hasNext()) {
        LOG.warn(
            "GraphQueryResult yielded several statements. Only parsing the first. Maybe you intended to expect a collection?");
      }
      return mapStatement(statement, schema.getProperties());
    } else {
      throw new EntityMapperRuntimeException("GraphQueryResult did not yield any values.");
    }
  }

  private Object mapStatement(Statement statement, Map<String, Property> properties) {

    return statement.getObject().stringValue();

  }

  private Object mapCollection(GraphEntity entity, ArrayProperty schema) {
    Property itemSchema = schema.getItems();

    if (itemSchema == null) {
      throw new EntityMapperRuntimeException("Array schemas must have an 'items' property.");
    }

    if (!(itemSchema instanceof ObjectProperty)) {
      throw new EntityMapperRuntimeException(
          "Only array items of type 'object' are supported for now.");
    }

    GraphQueryResult result = entity.getResult();

    ImmutableList.Builder<Map<String, Object>> collectionBuilder = new ImmutableList.Builder<>();
    Map<String, Property> itemProperties = ((ObjectProperty) itemSchema).getProperties();

    while (result.hasNext()) {
      collectionBuilder.add(mapStatements(result.next(), itemProperties));
    }

    return collectionBuilder.build();
  }

  private Map<String, Object> mapStatements(Statement statement,
      Map<String, Property> itemProperties) {
    ImmutableMap.Builder<String, Object> itemBuilder = new ImmutableMap.Builder<>();

    itemProperties.forEach((name, property) -> {
      if (property.getRequired() && statement.getObject() != null) {
        throw new EntityMapperRuntimeException(String.format("Property '%s' is required.", name));
      }


      itemBuilder.put(name, schemaMapperAdapter.mapTupleValue(property, statement.getObject()));
    });
    return itemBuilder.build();
  }



}
