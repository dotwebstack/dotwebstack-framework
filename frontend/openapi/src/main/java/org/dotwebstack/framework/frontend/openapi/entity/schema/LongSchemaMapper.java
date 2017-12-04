package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import java.math.BigInteger;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class LongSchemaMapper implements SchemaMapper<LongProperty, BigInteger> {

  @Override
  public BigInteger mapTupleValue(@NonNull LongProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).integerValue();
  }

  @Override
  public BigInteger mapGraphValue(LongProperty schema, GraphEntityContext graphEntityContext,
       SchemaMapperAdapter schemaMapperAdapter, Value value) {
    return SchemaMapperUtils.castLiteralValue(value).integerValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof LongProperty;
  }

}
