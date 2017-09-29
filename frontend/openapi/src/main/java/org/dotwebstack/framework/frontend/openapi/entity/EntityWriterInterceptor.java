package org.dotwebstack.framework.frontend.openapi.entity;

import java.io.IOException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

public final class EntityWriterInterceptor implements WriterInterceptor {

  private TupleEntityMapper tupleEntityMapper;

  @Autowired
  public EntityWriterInterceptor(@NonNull TupleEntityMapper tupleEntityMapper) {
    this.tupleEntityMapper = tupleEntityMapper;
  }

  @Override
  public void aroundWriteTo(@NonNull WriterInterceptorContext context) throws IOException {
    if (context.getEntity() instanceof TupleEntity) {
      TupleEntity entity = (TupleEntity) context.getEntity();
      Object mappedEntity = tupleEntityMapper.map(entity, context.getMediaType());
      context.setEntity(mappedEntity);
    }

    context.proceed();
  }

}
