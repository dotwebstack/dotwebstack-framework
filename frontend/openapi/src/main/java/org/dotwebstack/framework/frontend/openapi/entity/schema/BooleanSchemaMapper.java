package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.Property;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Service
class BooleanSchemaMapper extends AbstractSchemaMapper<BooleanProperty, Boolean> {

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.BOOLEAN);

  @Override
  public Boolean mapTupleValue(@NonNull BooleanProperty schema,
                               @NonNull ValueContext valueContext) {
    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).booleanValue();
  }

  @Override
  public Boolean mapGraphValue(@NonNull BooleanProperty property,
                               @NonNull GraphEntity graphEntity, @NonNull ValueContext valueContext,
                               @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    validateVendorExtensions(property);
    Map<String, Object> vendorExtensions = property.getVendorExtensions();

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
      return handleLdPathVendorExtension(property, valueContext.getValue(), ldPathExecutor);
    }

    return SchemaMapperUtils.castLiteralValue(valueContext.getValue()).booleanValue();
  }

  private Boolean handleLdPathVendorExtension(BooleanProperty property, Value context, LdPathExecutor ldPathExecutor) {
    String ldPathQuery =
            (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null) {
      if (property.getRequired()) {
        throw new SchemaMapperRuntimeException(String.format(
                "Boolean property has '%s' vendor extension that is null, but the property is required.",
                OpenApiSpecificationExtensions.LDPATH));
      }
      return null;
    }

    /* at this point we're sure that ld-path is not null */
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);
    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    return SchemaMapperUtils.castLiteralValue(getSingleStatement(queryResult, ldPathQuery)).booleanValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof BooleanProperty;
  }


  /**
   * Validates the vendor extensions that are declared on the BooleanProperty. A BooleanProperty
   * should have exactly one of these vendor extensions:
   * <ul>
   * <li>{@link OpenApiSpecificationExtensions#CONSTANT_VALUE}</li>
   * <li>{@link OpenApiSpecificationExtensions#LDPATH}</li>
   * </ul>
   *
   * @throws SchemaMapperRuntimeException if none of these or multiple of these vendor extensions
   *                                      are encountered.
   */
  private void validateVendorExtensions(BooleanProperty property) {

    ImmutableSet<String> supportedVendorExtensions = ImmutableSet.of(
            OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE);

    long nrOfSupportedVendorExtentionsPresent =
            property.getVendorExtensions().keySet().stream().filter(
                    supportedVendorExtensions::contains).count();
    if (nrOfSupportedVendorExtentionsPresent > 1) {
      throw new SchemaMapperRuntimeException(String.format(
              "A string object must have either no, a '%s' or '%s' property. "
                      + "A boolean object cannot have a combination of these.",
              OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.CONSTANT_VALUE));
    }
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }
}
