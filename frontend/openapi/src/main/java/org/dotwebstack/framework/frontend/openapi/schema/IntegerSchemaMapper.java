package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.BaseIntegerProperty;
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
class IntegerSchemaMapper extends AbstractSchemaMapper
    implements SchemaMapper<BaseIntegerProperty, Object> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.INTEGER, XMLSchema.INT);


  @Override
  public Object mapTupleValue(@NonNull BaseIntegerProperty schema, @NonNull Value value) {
    return SchemaMapperUtils.castLiteralValue(value).intValue();
  }

  @Override
  public Object mapGraphValue(BaseIntegerProperty property, GraphEntityContext context,
      SchemaMapperAdapter schemaMapperAdapter, Value value) {
    String ldPathQuery =
            (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isSupported(value)) {
      return ((Literal) value).integerValue().intValue();
    }

    if (ldPathQuery == null) {
      throw new SchemaMapperRuntimeException(
              String.format("Property '%s' must have a '%s' attribute.", property.getName(),
                      OpenApiSpecificationExtensions.LDPATH));
    }
    LdPathExecutor ldPathExecutor = context.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(value, ldPathQuery);

    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value integerValue = getSingleStatement(queryResult, ldPathQuery);

    if (!isSupported(integerValue)) {
      throw new SchemaMapperRuntimeException(String.format(
              "LDPath query '%s' yielded a value which is not a literal of supported type: <%s>.",
              ldPathQuery, dataTypesAsString()));
    }

    return ((Literal) integerValue).integerValue().intValue();
  }


  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }



  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BaseIntegerProperty;
  }

}
