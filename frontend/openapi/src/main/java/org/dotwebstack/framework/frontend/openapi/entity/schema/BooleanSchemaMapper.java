package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class BooleanSchemaMapper extends AbstractSchemaMapper<BooleanProperty, Boolean> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.BOOLEAN);
  private static final Set<String> SUPPORTED_VENDOR_EXTENSIONS = ImmutableSet.of(
      OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE);

  @Override
  public Boolean mapTupleValue(@NonNull BooleanProperty schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).booleanValue();
  }

  @Override
  public Boolean mapGraphValue(@NonNull BooleanProperty schema, @NonNull GraphEntity entity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    validateVendorExtensions(schema, SUPPORTED_VENDOR_EXTENSIONS);
    Map<String, Object> vendorExtensions = schema.getVendorExtensions();

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      LdPathExecutor ldPathExecutor = entity.getLdPathExecutor();
      return handleLdPathVendorExtension(schema, valueContext.getValue(), ldPathExecutor);
    }

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.CONSTANT_VALUE)) {
      return handleConstantValueVendorExtension(schema);
    }

    // TODO: line below should never be reached. Return null instead?
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).booleanValue();
  }

  @SuppressWarnings("squid:S2447")
  private Boolean handleLdPathVendorExtension(BooleanProperty property, Value context,
      LdPathExecutor ldPathExecutor) {
    String vendorExtension = OpenApiSpecificationExtensions.LDPATH;
    String ldPathQuery = (String) property.getVendorExtensions().get(vendorExtension);

    if (ldPathQuery == null) {
      handleRequired(property, vendorExtension);
      return null;
    }

    /* at this point we're sure that ld-path is not null */
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);
    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    return SchemaMapperUtils.castLiteralValue(
        getSingleStatement(queryResult, ldPathQuery)).booleanValue();
  }

  @SuppressWarnings("squid:S2447")
  private Boolean handleConstantValueVendorExtension(BooleanProperty property) {
    String vendorExtension = OpenApiSpecificationExtensions.CONSTANT_VALUE;
    Object value = property.getVendorExtensions().get(vendorExtension);

    if (value != null) {
      if (isSupportedLiteral(value)) {
        return SchemaMapperUtils.castLiteralValue((Value) value).booleanValue();
      }

      return Boolean.valueOf(value.toString());
    }

    handleRequired(property, vendorExtension);

    return null;
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BooleanProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

}
