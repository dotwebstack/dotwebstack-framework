package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class IntegerSchemaMapper extends AbstractSchemaMapper<BaseIntegerProperty, Object> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.INTEGER, XMLSchema.INT);

  @Override
  public Object mapTupleValue(@NonNull BaseIntegerProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).intValue();
  }

  @Override
  public Object mapGraphValue(@NonNull BaseIntegerProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    String ldPathQuery =
        (String) schema.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isSupportedLiteral(valueContext.getValue())) {
      return ((Literal) valueContext.getValue()).integerValue().intValue();
    }

    if (ldPathQuery == null) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' must have a '%s' attribute.", schema.getName(),
              OpenApiSpecificationExtensions.LDPATH));
    }
    LdPathExecutor ldPathExecutor = entity.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPathQuery);

    if (!schema.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value integerValue = getSingleStatement(queryResult, ldPathQuery);

    if (!isSupportedLiteral(integerValue)) {
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>.",
          ldPathQuery, dataTypesAsString()));
    }

    return ((Literal) integerValue).integerValue().intValue();
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BaseIntegerProperty;
  }

}
