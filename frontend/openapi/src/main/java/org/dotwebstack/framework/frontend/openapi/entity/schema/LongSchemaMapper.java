package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import java.math.BigInteger;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.springframework.stereotype.Service;

@Service
public class LongSchemaMapper implements SchemaMapper<LongProperty, BigInteger> {

  @Override
  public BigInteger mapTupleValue(@NonNull LongProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).integerValue();
  }

  @Override
  public BigInteger mapGraphValue(@NonNull LongProperty schema, @NonNull GraphEntity entity,
      @NotNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).integerValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof LongProperty;
  }

}
