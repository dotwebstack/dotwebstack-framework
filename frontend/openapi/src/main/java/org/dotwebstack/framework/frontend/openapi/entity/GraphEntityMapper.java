package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilder;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.properties.PropertyHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class GraphEntityMapper implements EntityMapper<GraphEntity> {

  private static final Logger LOG = LoggerFactory.getLogger(GraphEntityMapper.class);
  private final EntityBuilder entityBuilder;
  private final PropertyHandlerRegistry propertyHandlerRegistry;


  @Autowired
  public GraphEntityMapper(@NonNull EntityBuilder entityBuilder,
      @NonNull PropertyHandlerRegistry propertyHandlerRegistry) {

    this.entityBuilder = entityBuilder;
    this.propertyHandlerRegistry = propertyHandlerRegistry;
  }

  @Override
  public Object map(GraphEntity entity, MediaType mediaType) {
    Property schemaProperty = entity.getSchemaProperty();

    QueryResult result = entity.getQueryResult();
    LOG.debug("Building entity");
    EntityBuilderContext builderContext =
        new EntityBuilderContext.Builder().queryResult(result).build();
    return entityBuilder.build(schemaProperty, propertyHandlerRegistry, builderContext);
  }

}
