package org.dotwebstack.framework.backend.rdf4j;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class Rdf4jUtils {

  private Rdf4jUtils() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Rdf4jUtils.class));
  }

  /**
   * Convert literal to Java type so built-in scalar types can handle them.
   */
  public static Object convertLiteral(Literal literal) {
    IRI dataType = literal.getDatatype();

    if (XMLSchema.STRING.equals(dataType)) {
      return literal.stringValue();
    } else if (XMLSchema.BOOLEAN.equals(dataType)) {
      return literal.booleanValue();
    } else if (XMLSchema.INT.equals(dataType)) {
      return literal.intValue();
    } else if (XMLSchema.INTEGER.equals(dataType)) {
      return literal.integerValue();
    } else if (XMLSchema.SHORT.equals(dataType)) {
      return literal.shortValue();
    } else if (XMLSchema.LONG.equals(dataType)) {
      return literal.longValue();
    } else if (XMLSchema.FLOAT.equals(dataType)) {
      return literal.floatValue();
    } else if (XMLSchema.DOUBLE.equals(dataType)) {
      return literal.doubleValue();
    } else if (XMLSchema.DECIMAL.equals(dataType)) {
      return literal.decimalValue();
    } else if (XMLSchema.BYTE.equals(dataType)) {
      return literal.byteValue();
    }

    return literal;
  }

}
