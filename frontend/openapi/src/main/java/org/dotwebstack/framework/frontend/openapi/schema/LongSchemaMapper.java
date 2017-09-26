package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import java.math.BigInteger;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class LongSchemaMapper implements SchemaMapper<LongProperty, BigInteger> {

  @Override
  public BigInteger mapTupleValue(@NonNull LongProperty schema, @NonNull Value value) {
    if (!(value instanceof Literal)) {
      throw new SchemaHandlerRuntimeException(
          String.format("Schema '%s' is not a literal value.", schema.getName()));
    }

    return ((Literal) value).integerValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof LongProperty;
  }

}
