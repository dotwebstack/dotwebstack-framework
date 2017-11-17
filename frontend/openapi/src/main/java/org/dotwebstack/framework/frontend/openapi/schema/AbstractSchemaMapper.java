package org.dotwebstack.framework.frontend.openapi.schema;

import com.google.common.base.Joiner;

import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import jersey.repackaged.com.google.common.collect.ImmutableSet;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

public abstract class AbstractSchemaMapper {

  static Value getSingleStatement(Collection<Value> queryResult, String ldPathQuery) {

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

  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of();
  }

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

  /**
   * Checks if given value object is instance of {@link Literal} and its data type is one of those
   * provided by {@link #isDataTypeSupported(Literal)}.
   *
   * @param value value to check
   * @return <code>true</code> if given value is literal which supports one of given data types,
   *         <code>false</code> otherwise.
   */
  boolean isLiteral(Object value) {

    return value instanceof Literal && isDataTypeSupported((Literal) value);
  }

  @SuppressWarnings("unchecked")
  Map<String, Object> mapGraphResource(Property schemaProperty,
      GraphEntityContext entityBuilderContext, SchemaMapperAdapter schemaMapperAdapter,
      Value ldPathContext) {
    return (Map<String, Object>) schemaMapperAdapter.mapGraphValue(schemaProperty,
        entityBuilderContext, schemaMapperAdapter, ldPathContext);
  }

}
