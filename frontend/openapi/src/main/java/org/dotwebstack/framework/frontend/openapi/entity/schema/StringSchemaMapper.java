package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
public class StringSchemaMapper extends AbstractSchemaMapper<StringProperty, String> {

  static final String PATTERN = "pattern";
  static final String LINK_CHOICES = "link-choices";
  static final String KEY = "key";

  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.STRING, RDF.LANGSTRING);

  @Override
  public String mapTupleValue(@NonNull StringProperty schema, @NonNull ValueContext valueContext) {
    return valueContext.getValue().stringValue();
  }

  @Override
  public String mapGraphValue(@NonNull StringProperty property, @NonNull GraphEntity graphEntity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {
    validateVendorExtensions(property);
    Map<String, Object> vendorExtensions = property.getVendorExtensions();

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.RELATIVE_LINK)) {
      return handleRelativeLinkVendorExtension(
          (Map<String, String>) vendorExtensions.get(OpenApiSpecificationExtensions.RELATIVE_LINK),
          graphEntity, valueContext);
    }
    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.CONTEXT_LINKS)) {
      return handleContextLinkVendorExtension(property, graphEntity, valueContext);
    }
    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
      return handleLdPathVendorExtension(property, valueContext.getValue(), ldPathExecutor);
    }

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.CONSTANT_VALUE)) {
      return handleConstantValueVendorExtension(property);
    }

    if (valueContext.getValue() != null) {
      return valueContext.getValue().stringValue();
    } else if (property.getRequired()) {
      throw new SchemaMapperRuntimeException("No result for required property.");
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  private String handleContextLinkVendorExtension(StringProperty property, GraphEntity graphEntity,
      ValueContext valueContext) {

    Map<String, Object> contextLink = expectValue(property.getVendorExtensions(),
        OpenApiSpecificationExtensions.CONTEXT_LINKS, Map.class);

    List<Map<String, Object>> choices = expectValue(contextLink, LINK_CHOICES, List.class);

    /* this ldpath is used to get real value of key */
    String realValueLdPath =
        expectValue(contextLink, OpenApiSpecificationExtensions.KEY_LDPATH, String.class);
    LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
    Collection<Value> values = ldPathExecutor.ldPathQuery(valueContext.getValue(), realValueLdPath);
    String realValue = getSingleStatement(values, realValueLdPath).stringValue();

    if (realValue != null) {
      /* this ldpath is inherited by all choices */
      String linkCommonLdPath = (String) contextLink.get(OpenApiSpecificationExtensions.LDPATH);

      for (Map<String, Object> choice : (List<Map<String, Object>>) choices) {
        Object key = choice.get(KEY);
        if (realValue.equalsIgnoreCase(key.toString())) {
          Map<String, String> relativeLinkProperty = Maps.newHashMap(
              (Map<String, String>) choice.get(OpenApiSpecificationExtensions.RELATIVE_LINK));
          relativeLinkProperty.putIfAbsent(OpenApiSpecificationExtensions.LDPATH, linkCommonLdPath);

          return handleRelativeLinkVendorExtension(relativeLinkProperty, graphEntity, valueContext);
        }
      }
    }

    return null;
  }

  private <T> T expectValue(Map<String, Object> map, String key, Class<T> clazz) {
    Object value = map.get(key);

    if (!clazz.isInstance(value)) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' should be defined as %s.", key, clazz.getSimpleName()));
    }

    return clazz.cast(value);
  }

  private String handleRelativeLinkVendorExtension(Map<String, String> relativeLinkPropertiesMap,
      GraphEntity graphEntity, ValueContext valueContext) {

    if (relativeLinkPropertiesMap == null) {
      throw new SchemaMapperRuntimeException(String.format("Property '%s' can not be null.",
          OpenApiSpecificationExtensions.RELATIVE_LINK));
    }

    if (!relativeLinkPropertiesMap.containsKey(PATTERN)) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' should have a '%s' property.",
              OpenApiSpecificationExtensions.RELATIVE_LINK, PATTERN));
    }

    if (relativeLinkPropertiesMap.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      Collection<Value> queryResult =
          graphEntity.getLdPathExecutor().ldPathQuery(valueContext.getValue(),
              relativeLinkPropertiesMap.get(OpenApiSpecificationExtensions.LDPATH));

      if (queryResult.size() > 1) {
        throw new SchemaMapperRuntimeException(String.format(
            "LDPath query '%s' yielded multiple results (%d) for a property, which "
                + "requires a single result.",
            relativeLinkPropertiesMap.get(OpenApiSpecificationExtensions.LDPATH),
            queryResult.size()));
      }

      if (queryResult.isEmpty()) {
        return null;
      }

      return graphEntity.getBaseUri() + relativeLinkPropertiesMap.get(PATTERN).replace("$1",
          queryResult.iterator().next().stringValue());

    }

    return graphEntity.getBaseUri() + relativeLinkPropertiesMap.get(PATTERN);
  }

  /**
   * Validates the vendor extensions that are declared on the StringProperty. A StringProperty
   * should have exactly one of these vendor extensions:
   * <ul>
   * <li>{@link OpenApiSpecificationExtensions#CONSTANT_VALUE}</li>
   * <li>{@link OpenApiSpecificationExtensions#LDPATH}</li>
   * </ul>
   *
   * @throws SchemaMapperRuntimeException if none of these or multiple of these vendor extentions
   *         are encountered.
   */
  private void validateVendorExtensions(StringProperty property) {

    ImmutableSet<String> supportedVendorExtensions = ImmutableSet.of(
        OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RELATIVE_LINK,
        OpenApiSpecificationExtensions.CONSTANT_VALUE);

    long nrOfSupportedVendorExtentionsPresent =
        property.getVendorExtensions().keySet().stream().filter(
            supportedVendorExtensions::contains).count();
    if (nrOfSupportedVendorExtentionsPresent > 1) {
      throw new SchemaMapperRuntimeException(String.format(
          "A string object must have either no, a '%s', '%s' or '%s' property. "
              + "A string object cannot have a combination of these.",
          OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RELATIVE_LINK,
          OpenApiSpecificationExtensions.CONSTANT_VALUE));
    }
  }

  private String handleConstantValueVendorExtension(StringProperty property) {
    Object value =
        property.getVendorExtensions().get(OpenApiSpecificationExtensions.CONSTANT_VALUE);

    if (value != null) {
      if (isSupportedLiteral(value)) {
        return ((Value) value).stringValue();
      }

      return value.toString();
    }

    if (property.getRequired()) {
      throw new SchemaMapperRuntimeException(String.format(
          "String property has '%s' vendor extension that is null, but the property is required.",
          OpenApiSpecificationExtensions.CONSTANT_VALUE));
    }

    return null;
  }

  private String handleLdPathVendorExtension(StringProperty property, Value context,
      LdPathExecutor ldPathExecutor) {
    String ldPathQuery =
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null) {
      if (property.getRequired()) {
        throw new SchemaMapperRuntimeException(String.format(
            "String property has '%s' vendor extension that is null, but the property is required.",
            OpenApiSpecificationExtensions.LDPATH));
      }
      return null;
    }

    /* at this point we're sure that ld-path is not null */
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);
    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    return getSingleStatement(queryResult, ldPathQuery).stringValue();
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof StringProperty;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

}
