package org.dotwebstack.framework.frontend.openapi.entity;

import java.io.IOException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilder;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;
import org.dotwebstack.framework.frontend.openapi.entity.mapper.GraphEntityMapper;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public final class EntityWriterInterceptor implements WriterInterceptor {

  private final EntityBuilder entityBuilder;
  private final PropertyHandlerRegistry propertyHandlerRegistry;
  private TupleEntityMapper tupleEntityMapper;

  private GraphEntityMapper graphEntityMapper;

  @Autowired
  public EntityWriterInterceptor(@NonNull TupleEntityMapper tupleEntityMapper,
                                 @NonNull GraphEntityMapper graphEntityMapper,
                                 @NonNull EntityBuilder entityBuilder,
                                 PropertyHandlerRegistry handlersRegistry) {
    this.tupleEntityMapper = tupleEntityMapper;
    this.graphEntityMapper = graphEntityMapper;
    this.entityBuilder = entityBuilder;
    this.propertyHandlerRegistry=handlersRegistry;
  }

  @Override
  public void aroundWriteTo(@NonNull WriterInterceptorContext context) throws IOException {


    Entity entity = (Entity) context.getEntity();

    Property schemaProperty = entity.getSchemaProperty();

    RequestParameters requestParameters = entity.getRequestParameters();

    String baseUri = entity.getBaseUri();

    String endpoint = entity.getEndpoint();

//    String baseUri = baseUriFactory.newBaseUri(containerRequestContext);
//    String endpoint = endpointPath(containerRequestContext.getUriInfo());

    QueryResult result = entity.getQueryResult();
    EntityBuilderContext builderContext =
            new EntityBuilderContext.Builder(endpoint).queryResult(result).baseUri(
                    baseUri).requestParameters(requestParameters).build();
    Object entityRaw = entityBuilder.build(schemaProperty, propertyHandlerRegistry, builderContext);

    context.setEntity(entityRaw);


//
//
//    if (context.getEntity() instanceof TupleEntity) {
//      TupleEntity entity = (TupleEntity) context.getEntity();
//      Object mappedEntity = tupleEntityMapper.map(entity, context.getMediaType());
//      context.setEntity(mappedEntity);
//    }
//    if (context.getEntity() instanceof GraphEntity) {
//      GraphEntity entity = (GraphEntity) context.getEntity();
//      Object mappedEntity = graphEntityMapper.map(entity, context.getMediaType());
//      context.setEntity(mappedEntity);
//    }

    // Object == json Map

    context.proceed();
  }

}
