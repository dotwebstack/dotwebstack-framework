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

  // XXX (PvH) Wordt enkel gebruikt door de ObjectSchemaMapper, daar naar toeverplaatsen?
  // XXX (PvH) Ik vind de implementatie wat verwarrend, en vanuit de afnemer vind ik de method
  // lastig te interpreteren. De extension heet EXCLUDE_... en de method heet isIncluded... Kunnen
  // we dit niet synchroon houden, bijvoorbeeld:
  //
  // boolean excludeWhenNull(Property property, Object value) {
  // return value == null && hasVendorExtensionWithValue
  // }
  //
  // XXX (PvH) Waarom verwijzen we naar de ArrayProperty? En kan deze wel null zijn? (en niet enkel
  // leeg) Is het bijvoorbeeld niet beter dat we de method aanroepen als de property geen
  // ArrayProperty is?
  // XXX (PvH) Kunnen we hier een unit test voor schrijven?
  protected boolean isIncludedWhenNull(Property property, Object value) {
    return !(hasVendorExtensionWithValue(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_NULL, true) && (value == null)
        && !(property instanceof ArrayProperty));
  }

  // XXX (PvH) Zie hierboven
  // XXX (PvH) Waarom verwijzen we naar de ArrayProperty? Is het niet beter dat we de method
  // aanroepen als de property enkel een ArrayProperty is?
  protected boolean isIncludedWhenEmpty(Property property, Object value) {
    return !(hasVendorExtensionWithValue(property,
        OpenApiSpecificationExtensions.EXCLUDE_PROPERTIES_WHEN_EMPTY, true)
        && (value != null && ((Collection) value).isEmpty()
            && (property instanceof ArrayProperty)));
  }

  // XXX (PvH) Kunnen we de method private maken? De method wordt tenslotte enkel gebruikt door deze
  // klasse
  // XXX (PvH) Kunnen we hier een unit test voor schrijven?
  protected boolean hasVendorExtensionWithValue(Property property, String extension, Object value) {
    return hasVendorExtension(property, extension)
        && property.getVendorExtensions().get(extension).equals(value);
  }

  // XXX (PvH) Kunnen we hier een unit test voor schrijven?
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
