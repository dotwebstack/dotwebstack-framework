package org.dotwebstack.framework.frontend.openapi.schema;

import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import java.math.BigInteger;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
class LongSchemaMapper implements SchemaMapper<LongProperty, BigInteger> {

  @Override
  public BigInteger mapTupleValue(@NonNull LongProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).integerValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof LongProperty;
  }

}
