package org.dotwebstack.framework.frontend.openapi.entity.properties;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StringPropertyHandler extends AbstractPropertyHandler<StringProperty> {

  private static final Logger LOG = LoggerFactory.getLogger(StringPropertyHandler.class);
  static final String PATTERN = "pattern";
  static final String LINK_CHOICES = "link-choices";
  static final String KEY = "key";
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.STRING, RDF.LANGSTRING);

  @SuppressWarnings("unchecked")
  @Override
  public Object handle(StringProperty property, EntityBuilderContext entityBuilderContext,
      PropertyHandlerRegistry registry, Value context) {

    validateVendorExtensions(property);
    Map<String, Object> vendorExtensions = property.getVendorExtensions();
    if (vendorExtensions.containsKey(OasVendorExtensions.RELATIVE_LINK)) {
      return handleRelativeLinkVendorExtension(
          (Map<String, String>) vendorExtensions.get(OasVendorExtensions.RELATIVE_LINK),
          entityBuilderContext, context);
    }

    if (vendorExtensions.containsKey(OasVendorExtensions.CONTEXT_LINK)) {
      return handleContextLinkVendorExtension(property, entityBuilderContext, context);
    }

    if (vendorExtensions.containsKey(OasVendorExtensions.LDPATH)) {
      LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
      return handleLdPathVendorExtension(property, context, ldPathExecutor);
    }

    if (vendorExtensions.containsKey(OasVendorExtensions.CONSTANT_VALUE)) {
      return handleConstantValueVendorExtension(property);
    }

    if (context != null && isLiteral(context)) {
      return context.stringValue();
    } else if (property.getRequired()) {
      throw new PropertyHandlerRuntimeException("No result for required property.");
    }

    return null;
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return SUPPORTED_TYPES;
  }

  /**
   * Validates the vendor extensions that are declared on the StringProperty. A StringProperty
   * should have exactly one of these vendor extensions:
   * <ul>
   * <li>{@link OasVendorExtensions#CONSTANT_VALUE}</li>
   * <li>{@link OasVendorExtensions#LDPATH}</li>
   * <li>{@link OasVendorExtensions#RELATIVE_LINK}</li>
   * </ul>
   *
   * @throws PropertyHandlerRuntimeException if none of these or multiple of these vendor extentions
   *         are encountered.
   */
  private void validateVendorExtensions(StringProperty property) {

    ImmutableSet<String> supportedVendorExtensions = ImmutableSet.of(OasVendorExtensions.LDPATH,
        OasVendorExtensions.RELATIVE_LINK, OasVendorExtensions.CONSTANT_VALUE);

    long nrOfSupportedVendorExtentionsPresent =
        property.getVendorExtensions().keySet().stream().filter(
            supportedVendorExtensions::contains).count();
    if (nrOfSupportedVendorExtentionsPresent > 1) {
      throw new PropertyHandlerRuntimeException(String.format(
          "A string object must have either no, a '%s', '%s' or '%s' property. "
              + "A string object cannot have a combination of these.",
          OasVendorExtensions.LDPATH, OasVendorExtensions.RELATIVE_LINK,
          OasVendorExtensions.CONSTANT_VALUE));
    }
  }

  private Object handleConstantValueVendorExtension(StringProperty property) {
    Object value = property.getVendorExtensions().get(OasVendorExtensions.CONSTANT_VALUE);

    if (value != null) {
      if (isLiteral(value)) {
        return ((Value) value).stringValue();
      }

      return value.toString();
    }

    if (property.getRequired()) {
      throw new PropertyHandlerRuntimeException(String.format(
          "String property has '%s' vendor extension that is null, but the property is required.",
          OasVendorExtensions.CONSTANT_VALUE));
    }

    return null;
  }

  private Object handleLdPathVendorExtension(StringProperty property, Value context,
      LdPathExecutor ldPathExecutor) {
    String ldPathQuery = (String) property.getVendorExtensions().get(OasVendorExtensions.LDPATH);

    if (ldPathQuery == null) {
      if (property.getRequired()) {
        throw new PropertyHandlerRuntimeException(String.format(
            "String property has '%s' vendor extension that is null, but the property is required.",
            OasVendorExtensions.LDPATH));
      }
      return null;
    }

    /* at this point we're sure that ld-path is not null */
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(context, ldPathQuery);
    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    LOG.debug("Context: {}", context);
    return getSingleStatement(queryResult, ldPathQuery).stringValue();
  }

  private <T> T expectValue(Map<String, Object> map, String key, Class<T> clazz) {
    Object value = map.get(key);

    if (!clazz.isInstance(value)) {
      throw new PropertyHandlerRuntimeException(
          String.format("Property '%s' should be defined as %s.", key, clazz.getSimpleName()));
    }

    return clazz.cast(value);
  }

  /**
   * Handles "x-context-links" vendor extension. <br/>
   *
   * <pre>
   *   x-context-links:
   *     # (required) this ldpath is used to get real value of key
   *     x-key-ldpath: x / y / z
   *     # (optional) if x-relative-link does not declare its own ldpath, this one is used instead;
   *     # (handy, if almost all ldpaths are the same)
   *     x-ldpath: a / b /c
   *     # a list of key-value entries to choose from;
   *     link-choices:
   *       # value of the key is compared (case-insensitive) with result of x-key-ldpath
   *     - key: "bestemmingsplan"
   *       # this relative link is chosen if value of key above equals to result of x-key-ldpath
   *       x-relative-link:
   *         pattern: /bestemmingsplangebieden/$1
   *         x-ldpath: 'path'
   * </pre>
   *
   * @param property parent string property
   * @param entityBuilderContext provided entity context
   * @param context value context
   * @return the same as
   *         {@link #handleRelativeLinkVendorExtension(Map, EntityBuilderContext, Value)}
   */
  @SuppressWarnings("unchecked")
  private Object handleContextLinkVendorExtension(StringProperty property,
      EntityBuilderContext entityBuilderContext, Value context) {

    LOG.debug("Processing context {}...", context);

    Map<String, Object> contextLink =
        expectValue(property.getVendorExtensions(), OasVendorExtensions.CONTEXT_LINK, Map.class);

    List<Map<String, Object>> choices = expectValue(contextLink, LINK_CHOICES, List.class);

    /* this ldpath is used to get real value of key */
    String realValueLdPath = expectValue(contextLink, OasVendorExtensions.KEY_LDPATH, String.class);
    LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
    Collection<Value> values = ldPathExecutor.ldPathQuery(context, realValueLdPath);
    String realValue = getSingleStatement(values, realValueLdPath).stringValue();

    if (realValue != null) {
      /* this ldpath is inherited by all choices */
      String linkCommonLdPath = (String) contextLink.get(OasVendorExtensions.LDPATH);

      for (Map<String, Object> choice : (List<Map<String, Object>>) choices) {
        Object key = choice.get(KEY);
        if (realValue.equalsIgnoreCase(key.toString())) {
          Map<String, String> relativeLinkProperty =
              Maps.newHashMap((Map<String, String>) choice.get(OasVendorExtensions.RELATIVE_LINK));
          relativeLinkProperty.putIfAbsent(OasVendorExtensions.LDPATH, linkCommonLdPath);

          return handleRelativeLinkVendorExtension(relativeLinkProperty, entityBuilderContext,
              context);
        }
      }
    }

    return null;
  }

  private Object handleRelativeLinkVendorExtension(Map<String, String> relativeLinkProperty,
      EntityBuilderContext entityBuilderContext, Value context) {

    if (relativeLinkProperty == null) {
      throw new PropertyHandlerRuntimeException(
          String.format("Property '%s' can not be null.", OasVendorExtensions.RELATIVE_LINK));
    }

    if (!relativeLinkProperty.containsKey(PATTERN)) {
      throw new PropertyHandlerRuntimeException(
          String.format("Property '%s' should have a '%s' property.",
              OasVendorExtensions.RELATIVE_LINK, PATTERN));
    }

    if (relativeLinkProperty.containsKey(OasVendorExtensions.LDPATH)) {
      LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
      Collection<Value> queryResult =
          ldPathExecutor.ldPathQuery(context, relativeLinkProperty.get(OasVendorExtensions.LDPATH));

      if (queryResult.size() > 1) {
        throw new PropertyHandlerRuntimeException(String.format(
            "LDPath query '%s' yielded multiple results (%d) for a property, which "
                + "requires a single result.",
            relativeLinkProperty.get(OasVendorExtensions.LDPATH), queryResult.size()));
      }

      if (queryResult.isEmpty()) {
        return null;
      }

      return entityBuilderContext.getBaseUri() + relativeLinkProperty.get(PATTERN).replace("$1",
          queryResult.iterator().next().stringValue());

    }

    return entityBuilderContext.getBaseUri() + relativeLinkProperty.get(PATTERN);
  }

  @Override
  public boolean supports(Property property) {
    return property.getClass().equals(StringProperty.class);
  }

}
