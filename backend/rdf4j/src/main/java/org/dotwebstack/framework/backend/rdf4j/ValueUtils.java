package org.dotwebstack.framework.backend.rdf4j;

import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.SequencePath;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;

public final class ValueUtils {

  private ValueUtils() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", ValueUtils.class));
  }

  /**
   * Convert value to Java type so built-in scalar types can handle them.
   */
  public static Object convertValue(@NonNull Value value) {
    if (!(value instanceof Literal)) {
      return value;
    }

    Literal literal = (Literal) value;
    IRI dataType = literal.getDatatype();

    if (XMLSchema.BOOLEAN.equals(dataType)) {
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

    return literal.stringValue();
  }

  public static PropertyPath createPropertyPath(Model model, Resource subject, IRI predicate) {
    Value v = findRequiredProperty(model, subject, predicate);
    if (v instanceof MemBNode) {
      return SequencePath.builder().blankNode((MemBNode) v).predicateIri(predicate).build();
    }
    return PredicatePath.builder().iri((IRI) v).build();
  }

  public static Value findRequiredProperty(Model model, Resource subject, IRI predicate) {
    return Models.getProperty(model, subject, predicate)
        .orElseThrow(() -> new InvalidConfigurationException(String
            .format("Resource '%s' requires a '%s' IRI property.", subject, predicate)));
  }

  public static Literal findRequiredPropertyLiteral(Model model, Resource subject,
                                                    IRI predicate) {
    return Models.getPropertyLiteral(model, subject, predicate)
        .orElseThrow(() -> new InvalidConfigurationException(
            "Resource '{}' requires a '{}' literal property.", subject, predicate));
  }

}
