package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.base.Joiner;
import java.util.Collection;
import java.util.Set;
import jersey.repackaged.com.google.common.collect.ImmutableSet;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

// XXX (PvH) Kan package private worden
// XXX (PvH) Moet deze klasse SchemaMapper niet implementeren?
public abstract class AbstractSchemaMapper extends LdPathSchemaMapper {

  // XXX (PvH) @NonNull annotaties ontbreken. Miss goed om de volledige change set er op te
  // controleren.
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

  // XXX (PvH) Returned altijd een lege set?
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

  // XXX (PvH) Ik vond isLiteral voor de method naam beter, dat dekt meer de lading
  // XXX (PvH) Er wordt verwezen in de Javadoc naar een private method, dat lijkt me niet de
  // bedoeling.
  /**
   * Checks if given value object is instance of {@link Literal} and its data type is one of those
   * provided by {@link #isDataTypeSupported(Literal)}.
   *
   * @param value value to check
   * @return <code>true</code> if given value is literal which supports one of given data types,
   *         <code>false</code> otherwise.
   */
  boolean isSupported(Object value) {

    return value instanceof Literal && isDataTypeSupported((Literal) value);
  }


}
