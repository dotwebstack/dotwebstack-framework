package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Joiner;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;


abstract class AbstractSchemaMapper {

  static Value getSingleStatement(@NonNull Collection<Value> queryResult,
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

  String dataTypesAsString() {
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

  boolean isInclusedWhenNull(Property propValue, Object propertyResult) {
    return !(hasVendorExtensionWithValue(propValue,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_NULL, true)
        && (propertyResult == null) && !(propValue instanceof ArrayProperty));
  }

  boolean isInclusedWhenEmpty(Property propValue, Object propertyResult) {
    return !(hasVendorExtensionWithValue(propValue,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY, true)
        && (propertyResult != null && ((Collection) propertyResult).isEmpty()
            && (propValue instanceof ArrayProperty)));
  }

  boolean hasVendorExtensionWithValue(Property property, String extension, Object value) {
    return hasVendorExtension(property, extension)
        && property.getVendorExtensions().get(extension).equals(value);
  }

  boolean hasVendorExtension(Property property, String extension) {
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
  boolean isSupportedLiteral(Object value) {
    return value instanceof Literal && isDataTypeSupported((Literal) value);
  }

}
