package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public abstract class AbstractSchemaMapper<S extends Property, T> implements SchemaMapper<S, T> {

  // XXX: Subklassen kunnen ook in grid-frontend zitten. Kijk dus even goed naar de accessibility
  // van alle methods.

  @Override
  public T mapTupleValue(@NonNull S schema, @NonNull ValueContext valueContext) {
    return convertToType(SchemaMapperUtils.castLiteralValue(valueContext.getValue()));
  }

  @Override
  public T mapGraphValue(@NonNull S property, @NonNull GraphEntity graphEntity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {

    Map<String, Object> vendorExtensions = property.getVendorExtensions();

    // XXX: Alle drie worden ondersteund door de klassen die deze method gebruiken?
    ImmutableSet<String> supportedVendorExtensions = ImmutableSet.of(
        OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RELATIVE_LINK,
        OpenApiSpecificationExtensions.CONSTANT_VALUE);

    long nrOfSupportedVendorExtentionsPresent =
        property.getVendorExtensions().keySet().stream().filter(
            supportedVendorExtensions::contains).count();

    // XXX: Er is al een validateVendorExtensions method. Kan je deze hier gebruiken?
    // XXX: En wat als er 0 nrOfSupportedVendorExtentionsPresent zijn?
    if (nrOfSupportedVendorExtentionsPresent > 1) {
      throw new SchemaMapperRuntimeException(String.format(
          "A " + property.getType() + " object must have either no, a '%s', '%s' or '%s' property."
              + " A " + property.getType() + " object cannot have a combination of these.",
          OpenApiSpecificationExtensions.LDPATH, OpenApiSpecificationExtensions.RELATIVE_LINK,
          OpenApiSpecificationExtensions.CONSTANT_VALUE));
    }

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      return handleLdPathVendorExtension(property, valueContext, graphEntity);
    }
    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.CONSTANT_VALUE)) {
      return handleConstantValueVendorExtension(property);
    }
    // XXX: En de RELATIVE_LINK vendor extension?
    return null;
  }

  T handleConstantValueVendorExtension(S schema) {
    Object value = schema.getVendorExtensions().get(OpenApiSpecificationExtensions.CONSTANT_VALUE);

    if (value != null) {
      if (isSupportedLiteral(value)) {
        return convertToType(((Literal) value));
      }
      // XXX: Heb je bij iedere aanroep van deze methode een nieuwe ValueFactory nodig?
      ValueFactory valueFactory = SimpleValueFactory.getInstance();
      // XXX: XMLSchema.DATETIME?
      Literal literal = valueFactory.createLiteral((String) value, XMLSchema.DATETIME);
      return convertToType(literal);
    }

    // XXX: Hoeveel wijkt dit af van de handleRequired method?
    if (schema.getRequired()) {
      throw new SchemaMapperRuntimeException(String.format(
          "String Property has '%s' vendor extension that is null, but the property is required.",
          OpenApiSpecificationExtensions.CONSTANT_VALUE));
    }
    return null;
  }

  private T handleLdPathVendorExtension(@NonNull S property, @NonNull ValueContext valueContext,
      @NonNull GraphEntity graphEntity) {
    String ldPathQuery =
        (String) property.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null && isSupportedLiteral(valueContext.getValue())) {
      return convertToType(((Literal) valueContext.getValue()));
    }

    if (ldPathQuery == null) {
      throw new SchemaMapperRuntimeException(
          String.format("Property '%s' must have a '%s' attribute.", property.getName(),
              OpenApiSpecificationExtensions.LDPATH));
    }

    LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPathQuery);

    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value value = getSingleStatement(queryResult, ldPathQuery);
    try {
      return convertToType((Literal) value);
    } catch (IllegalArgumentException iae) {
      throw new SchemaMapperRuntimeException(expectedException(ldPathQuery));
    }
  }

  String expectedException(String ldPathQuery) {
    return ldPathQuery;
  }

  abstract T convertToType(Literal literal);

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

  /**
   * Validates the vendor extensions that are declared on the Property. A Property should have
   * exactly one of the vendor extensions declared in supportedExtensions
   *
   * @throws SchemaMapperRuntimeException if none of these or multiple of these vendor extensions
   *         are encountered.
   */
  void validateVendorExtensions(Property property, Set<String> supportedExtensions) {
    if (property.getVendorExtensions().keySet().stream().filter(
        supportedExtensions::contains).count() != 1) {

      String message = property.getClass().getSimpleName() + " object must have one of: "
          + supportedExtensions.toString().replaceAll("[\\[\\]]", "'").replaceAll(", ", "', '")
          + ". This object cannot have a combination of these.";

      throw new SchemaMapperRuntimeException(message);
    }
  }

  protected abstract Set<IRI> getSupportedDataTypes();

  String dataTypesAsString() {
    return Joiner.on(", ").join(getSupportedDataTypes());
  }

  private boolean isDataTypeSupported(Literal value) {
    if (value == null) {
      return false;
    }

    // XXX: Streamify?
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
  boolean isSupportedLiteral(Object value) {
    return value instanceof Literal && isDataTypeSupported((Literal) value);
  }

  void handleRequired(Property property, String vendorExtension) {
    if (property.getRequired()) {
      String message =
          String.format("%s has '%s' vendor extension that is null, but the property is required.",
              property.getClass().getSimpleName(), vendorExtension);
      throw new SchemaMapperRuntimeException(message);
    }
  }

}
