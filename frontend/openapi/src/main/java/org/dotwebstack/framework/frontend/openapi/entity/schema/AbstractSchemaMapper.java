package org.dotwebstack.framework.frontend.openapi.entity.schema;

import io.swagger.v3.oas.models.media.Schema;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public abstract class AbstractSchemaMapper<S extends Schema, T> implements SchemaMapper<S, T> {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  protected abstract Set<String> getSupportedVendorExtensions();

  @Override
  public T mapTupleValue(@NonNull S schema, @NonNull TupleEntity entity,
      @NonNull ValueContext valueContext) {
    return convertLiteralToType(SchemaMapperUtils.castLiteralValue(valueContext.getValue()));
  }

  @Override
  public T mapGraphValue(@NonNull S property, @NonNull GraphEntity graphEntity,
      @NonNull ValueContext valueContext, @NonNull SchemaMapperAdapter schemaMapperAdapter) {

    Map<String, Object> vendorExtensions = property.getExtensions();
    validateVendorExtensions(property, getSupportedVendorExtensions());

    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.LDPATH)) {
      return handleLdPathVendorExtension(property, valueContext, graphEntity);
    }
    if (vendorExtensions.containsKey(OpenApiSpecificationExtensions.CONSTANT_VALUE)) {
      return handleConstantValueVendorExtension(property);
    }
    throw new IllegalStateException(String.format(
        "Property %s cannot be mapped, no LDpath or ConstantValue defined.", property.toString()));
  }

  T handleConstantValueVendorExtension(S property) {
    Object value =
        property.getExtensions().get(OpenApiSpecificationExtensions.CONSTANT_VALUE);

    validateRequired(property, OpenApiSpecificationExtensions.CONSTANT_VALUE, value);

    if (value == null) {
      return null;
    }

    Literal literal;
    if (value instanceof Literal) {
      literal = (Literal) value;
    } else {
      literal =
          VALUE_FACTORY.createLiteral(value.toString(), getSupportedDataTypes().iterator().next());
    }

    return convertToType(literal);
  }

  T handleLdPathVendorExtension(@NonNull S property, @NonNull ValueContext valueContext,
      @NonNull GraphEntity graphEntity) {
    String ldPathQuery =
        (String) property.getExtensions().get(OpenApiSpecificationExtensions.LDPATH);

    if (ldPathQuery == null) {
      return handleLdPathVendorExtensionWithoutLdPath(property, valueContext);
    }

    LdPathExecutor ldPathExecutor = graphEntity.getLdPathExecutor();
    Collection<Value> queryResult =
        ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPathQuery);

    if (!property.getRequired() && queryResult.isEmpty()) {
      return null;
    }

    Value value = getSingleStatement(queryResult, ldPathQuery);

    try {
      return convertToType(value);
    } catch (RuntimeException ex) {
      throw new SchemaMapperRuntimeException(String.format(
          "[%s] LDPathQuery '%s' yielded a value which is not a literal of supported type <%s>",
          this.getClass().getSimpleName(), ldPathQuery, getSupportedDataTypes()), ex);
    }
  }

  private T handleLdPathVendorExtensionWithoutLdPath(@NonNull S property,
      @NonNull ValueContext valueContext) {
    validateRequired(property, OpenApiSpecificationExtensions.LDPATH, valueContext.getValue());

    if (isSupportedLiteral(valueContext.getValue())) {
      return convertToType((valueContext.getValue()));
    }
    return null;
  }

  private T convertToType(Value value) {
    if (isSupportedLiteral(value)) {
      return convertLiteralToType((Literal) value);
    } else {
      return convertValueToType(value);
    }
  }

  protected abstract T convertLiteralToType(Literal literal);

  protected T convertValueToType(Value value) {
    throw new UnsupportedOperationException();
  }

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
  private void validateVendorExtensions(Schema schema, Set<String> supportedExtensions) {
    if (schema.getExtensions().keySet().stream().filter(
        supportedExtensions::contains).count() != 1) {

      String message = schema.getClass().getSimpleName() + " object must have one of: "
          + supportedExtensions.toString().replaceAll("[\\[\\]]", "'").replaceAll(", ", "', '")
          + ". This object cannot have a combination of these.";

      throw new SchemaMapperRuntimeException(message);
    }
  }

  protected abstract Set<IRI> getSupportedDataTypes();

  protected boolean isDataTypeSupported(Literal value) {
    return value != null
        && getSupportedDataTypes().stream().anyMatch(x -> x.equals(value.getDatatype()));
  }

  protected static boolean hasVendorExtensionWithValue(@NonNull Schema schema,
      @NonNull String extension, Object value) {
    return hasVendorExtension(schema, extension)
        && schema.getExtensions().get(extension).equals(value);
  }

  protected static boolean hasVendorExtension(@NonNull Schema schema,
      @NonNull String extension) {
    return schema.getExtensions().containsKey(extension);
  }

  /**
   * Checks if given value object is instance of {@link Literal} and its data type is one of those
   * provided by {@link #getSupportedDataTypes()}.
   *
   * @param value value to check
   * @return <code>true</code> if given value is literal which supports one of given data types,
   *         <code>false</code> otherwise.
   */
  private boolean isSupportedLiteral(Value value) {
    return value instanceof Literal && isDataTypeSupported((Literal) value);
  }

  private void validateRequired(Schema schema, String vendorExtension, Object value) {
    if (value == null && schema.getRequired()) {
      String message =
          String.format("%s has '%s' vendor extension that is null, but the property is required.",
              schema.getClass().getSimpleName(), vendorExtension);
      throw new SchemaMapperRuntimeException(message);
    }
  }

}
