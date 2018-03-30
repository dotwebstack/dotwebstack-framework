package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Joiner;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

public abstract class AbstractSchemaMapper<S extends Property, T> implements SchemaMapper<S, T> {

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

  /**
   * Validates the vendor extensions that are declared on the Property. A Property
   * should have exactly one of the vendor extensions declared in supportedExtensions
   *
   * @throws SchemaMapperRuntimeException if none of these or multiple of these vendor extensions
   *         are encountered.
   */
  protected void validateVendorExtensions(Property property, Set<String> supportedExtensions) {
    if (property.getVendorExtensions().keySet().stream().filter(
            supportedExtensions::contains).count() != 1) {

      String message = property.getClass().getSimpleName()
          + " object must have one of: "
          + supportedExtensions.toString()
              .replaceAll("[\\[\\]]", "'")
              .replaceAll(", ", "', '")
          + ". This object cannot have a combination of these.";

      throw new SchemaMapperRuntimeException(message);
    }
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

  protected static boolean hasVendorExtensionWithValue(@NonNull Property property,
      @NonNull String extension, Object value) {
    return hasVendorExtension(property, extension)
        && property.getVendorExtensions().get(extension).equals(value);
  }

  protected static boolean hasVendorExtension(@NonNull Property property,
      @NonNull String extension) {
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

  protected void handleRequired(Property property, String vendorExtension) {
    if (property.getRequired()) {
      String message =
          String.format("%s has '%s' vendor extension that is null, but the property is required.",
              property.getClass().getSimpleName(), vendorExtension);
      throw new SchemaMapperRuntimeException(message);
    }
  }

}
