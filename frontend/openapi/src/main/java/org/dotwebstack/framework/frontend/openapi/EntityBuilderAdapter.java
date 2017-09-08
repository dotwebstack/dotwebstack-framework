package org.dotwebstack.framework.frontend.openapi;

import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityBuilderAdapter implements EntityBuilder<Object> {

  private TupleEntityBuilder tupleEntityBuilder;

  @Autowired
  public EntityBuilderAdapter(@NonNull TupleEntityBuilder tupleEntityBuilder) {
    this.tupleEntityBuilder = tupleEntityBuilder;
  }

  public Object build(@NonNull Object result, @NonNull Property schema) {
    if (result instanceof TupleQueryResult) {
      return tupleEntityBuilder.build((TupleQueryResult) result, schema);
    }

    throw new EntityBuilderRuntimeException(
        String.format("Result type '%s' is not supported.", result.getClass()));
  }

}
