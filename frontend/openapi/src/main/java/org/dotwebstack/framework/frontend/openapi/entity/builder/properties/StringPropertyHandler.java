package org.dotwebstack.framework.frontend.openapi.entity.builder.properties;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
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
  private static final Set<IRI> SUPPORTED_TYPES = ImmutableSet.of(XMLSchema.STRING, RDF.LANGSTRING);

  @SuppressWarnings("unchecked")
  @Override
  public Object handle(StringProperty property, EntityBuilderContext entityBuilderContext,
      PropertyHandlerRegistry registry, Value context) {

    validateVendorExtensions(property);

    Map<String, Object> vendorExtensions = property.getVendorExtensions();



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

  @Override
  public boolean supports(Property property) {
    return property.getClass().equals(StringProperty.class);
  }

}
