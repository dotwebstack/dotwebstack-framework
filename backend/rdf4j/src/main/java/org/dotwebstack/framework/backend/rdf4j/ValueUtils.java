package org.dotwebstack.framework.backend.rdf4j;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class ValueUtils {

  private ValueUtils() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", ValueUtils.class));
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

  public static IRI findIri(Model model, Resource subject, IRI predicate) {
    return Models.getPropertyIRI(model, subject, predicate)
        .orElseThrow(() -> new InvalidConfigurationException(String
            .format("Node shape '%s' requires a '%s' IRI property.", subject.stringValue(),
                predicate.stringValue())));
  }

  public static Literal findLiteral(Model model, Resource subject, IRI predicate) {
    return Models.getPropertyLiteral(model, subject, predicate)
        .orElseThrow(() -> new InvalidConfigurationException(String
            .format("Node shape '%s' requires a '%s' literal property.", subject.stringValue(),
                predicate.stringValue())));
  }

}
