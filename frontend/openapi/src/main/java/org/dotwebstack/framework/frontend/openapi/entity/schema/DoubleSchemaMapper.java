package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class DoubleSchemaMapper extends AbstractSchemaMapper<DoubleProperty, Double> {

  private static final Set<IRI> SUPPORTED_TYPES =
      ImmutableSet.of(XMLSchema.DOUBLE, XMLSchema.DOUBLE);

  @Override
  public Double mapTupleValue(@NonNull DoubleProperty schema,
      @NonNull SchemaMapperContext schemaMapperContext) {
    return SchemaMapperUtils.castLiteralValue(schemaMapperContext.getValue()).doubleValue();
  }

  @Override
  public Double mapGraphValue(DoubleProperty property, GraphEntityContext graphEntityContext,
      SchemaMapperContext schemaMapperContext, SchemaMapperAdapter schemaMapperAdapter) {
    String ldPathQuery =
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isSupportedLiteral(schemaMapperContext.getValue())) {
      return ((Literal) schemaMapperContext.getValue()).doubleValue();
    }

    if (ldPathQuery == null) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' must have a '%s' attribute.", property.getName(),
              OpenApiSpecificationExtensions.LDPATH));
    }
    LdPathExecutor ldPathExecutor = graphEntityContext.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(schemaMapperContext.getValue(), ldPathQuery);

    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value doubleValue = getSingleStatement(queryResult, ldPathQuery);

    if (!isSupportedLiteral(doubleValue)) {
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>.",
          ldPathQuery, dataTypesAsString()));
    }

    return ((Literal) doubleValue).doubleValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof DoubleProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }
}
