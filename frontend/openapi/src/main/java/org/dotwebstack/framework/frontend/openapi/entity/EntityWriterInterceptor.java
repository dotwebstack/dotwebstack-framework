package org.dotwebstack.framework.frontend.openapi.entity;

import java.io.IOException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.mapper.GraphEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;

public final class EntityWriterInterceptor implements WriterInterceptor {

  private TupleEntityMapper tupleEntityMapper;

  private GraphEntityMapper graphEntityMapper;

  @Autowired
  public EntityWriterInterceptor(@NonNull TupleEntityMapper tupleEntityMapper,
      @NonNull GraphEntityMapper graphEntityMapper) {
    this.tupleEntityMapper = tupleEntityMapper;
    this.graphEntityMapper = graphEntityMapper;
  }

  @Override
  public void aroundWriteTo(@NonNull WriterInterceptorContext context) throws IOException {
    if (context.getEntity() instanceof TupleEntity) {
      TupleEntity entity = (TupleEntity) context.getEntity();
      Object mappedEntity = tupleEntityMapper.map(entity, context.getMediaType());
      context.setEntity(mappedEntity);
    }
    if (context.getEntity() instanceof GraphEntity) {
      GraphEntity entity = (GraphEntity) context.getEntity();
      Object mappedEntity = graphEntityMapper.map(entity, context.getMediaType());
      context.setEntity(mappedEntity);
    }

    // Object == json Map

    context.proceed();
  }

}
