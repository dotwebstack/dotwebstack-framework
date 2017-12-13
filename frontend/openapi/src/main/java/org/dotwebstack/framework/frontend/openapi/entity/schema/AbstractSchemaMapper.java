package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Joiner;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ValueContext.ValueContextBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

// XXX (PvH) Unit test je de toegevoegde methods bewust niet? (vanaf processPropagationsInitial)
// Omdat we hier testdekking missen.
// XXX (PvH) Toegevoegde methods kunnen ook static worden. Hiermee communiceer je richting de
// afnemers dat dit een utility method is, die geen state gebruikt
abstract class AbstractSchemaMapper<S extends Property, T> implements SchemaMapper<S, T> {

  protected static Value getSingleStatement(@NonNull Collection<Value> queryResult,
      @NonNull String ldPathQuery) {

    if (queryResult.isEmpty()) {
      throw new SchemaMapperRuntimeException(
          String.format("No results for LDPath query '%s' for required property.", ldPathQuery));
    }

    if (queryResult.size() > 1) {
      throw new SchemaMapperRuntimeException(
          String.format("LDPath query '%s' yielded multiple results (%s) for a property, which "
              + "requires a single result.", ldPathQuery, queryResult.size()));
    }

    return queryResult.iterator().next();
  }

  protected abstract Set<IRI> getSupportedDataTypes();

  protected String dataTypesAsString() {
    return Joiner.on(", ").join(getSupportedDataTypes());
  }

  private boolean isDataTypeSupported(Literal value) {
    if (value == null) {
      return false;
    }

    IRI literalDataType = value.getDatatype();
    for (IRI dt : getSupportedDataTypes()) {
      if (literalDataType.equals(dt)) {
        return true;
      }
    }

    return false;
  }

  // XXX (PvH) Kunnen we de naam verbeteren? Suggestie: populateContextWithVendorExtensions
  protected ValueContext processPropagationsInitial(Property property, ValueContext valueContext) {
    ValueContextBuilder builder = valueContext.toBuilder();

    if (hasVendorExtension(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL)) {
      builder.isExcludedWhenEmptyOrNull(
          hasVendorExtensionExcludePropertiesWhenEmptyOrNull(property));
    }

    return builder.build();
  }

  protected boolean isExcludedWhenEmptyOrNull(ValueContext context, Property property,
      Object value) {
    return context.isExcludedWhenEmptyOrNull()
        && (value == null || (property instanceof ArrayProperty && ((Collection) value).isEmpty()));
  }

  private boolean hasVendorExtensionExcludePropertiesWhenEmptyOrNull(Property propValue) {
    return (hasVendorExtensionWithValue(propValue,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY_OR_NULL, true));

  }

  protected boolean hasVendorExtensionWithValue(Property property, String extension, Object value) {
    return hasVendorExtension(property, extension)
        && property.getVendorExtensions().get(extension).equals(value);
  }

  protected boolean hasVendorExtension(Property property, String extension) {
    return property.getVendorExtensions().containsKey(extension);
  }

  /**
   * Checks if given value object is instance of {@link Literal} and its data type is one of those
   * provided by {@link #getSupportedDataTypes()}.
   *
   * @param value value to check
   * @return <code>true</code> if given value is literal which supports one of given data types,
   *         <code>false</code> otherwise.
   */
  protected boolean isSupportedLiteral(Object value) {
    return value instanceof Literal && isDataTypeSupported((Literal) value);
  }

}
