package org.dotwebstack.framework.backend.rdf4j.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class CompareHelper {

  private CompareHelper() {}

  public static Comparator<Value> getComparator(boolean asc) {
    return (value1, value2) -> compareValue(value1, value2, asc);
  }

  public static Comparator<Value> getComparator(boolean asc, @NonNull Model model, @NonNull String field,
      @NonNull NodeShape nodeShape) {
    return (value1, value2) -> compareOptional(resolveValue(value1, model, field, nodeShape),
        resolveValue(value2, model, field, nodeShape), asc);
  }

  private static Optional<Value> resolveValue(Value value, Model model, String path, NodeShape nodeShape) {
    List<String> fields = new ArrayList<>(Arrays.asList(path.split("\\.")));
    String field = fields.remove(0);
    PropertyShape propertyShape = nodeShape.getPropertyShape(field);

    IRI iri = propertyShape.getPath()
        .getBaseIri();
    if (propertyShape.getPath() instanceof PredicatePath) {
      iri = ((PredicatePath) propertyShape.getPath()).getIri();
    }

    Optional<Value> childOptional = Models.getProperty(model, (Resource) value, iri);
    if (childOptional.isPresent()) {
      if (fields.isEmpty()) {
        return childOptional;
      } else {
        return childOptional
            .flatMap(childValue -> resolveValue(childValue, model, String.join(".", field), propertyShape.getNode()));
      }
    }

    return Optional.empty();
  }

  private static int compareOptional(Optional<Value> optional1, Optional<Value> optional2, boolean asc) {
    if (optional1.isPresent() && optional2.isPresent()) {
      return compareValue(optional1.get(), optional2.get(), asc);
    }

    // sort in comparison to null values
    return optional1.isEmpty() ? 1 : -1;
  }

  private static int compareValue(@NonNull Value value1, @NonNull Value value2, boolean asc) {

    IRI datatype = ((SimpleLiteral) value1).getDatatype();
    if (isInteger(datatype)) {
      return compareIntegerLiteral((SimpleLiteral) value1, (SimpleLiteral) value2, asc);
    }

    if (isDecimal(datatype)) {
      return compareDecimalLiteral((SimpleLiteral) value1, (SimpleLiteral) value2, asc);
    }
    return compareStringValue(value1, value2, asc);
  }

  private static int compareStringValue(Value value1, Value value2, boolean asc) {
    if (asc) {
      return value1.stringValue()
          .compareTo(value2.stringValue());
    }
    return value2.stringValue()
        .compareTo(value1.stringValue());
  }

  private static int compareIntegerLiteral(SimpleLiteral integerLiteral1, SimpleLiteral integerLiteral2, boolean asc) {
    if (asc) {
      return integerLiteral1.integerValue()
          .compareTo(integerLiteral2.integerValue());
    }
    return integerLiteral2.integerValue()
        .compareTo(integerLiteral1.integerValue());
  }

  private static int compareDecimalLiteral(SimpleLiteral integerLiteral1, SimpleLiteral integerLiteral2, boolean asc) {
    if (asc) {
      return Float.compare(integerLiteral1.floatValue(), integerLiteral2.floatValue());
    }
    return Float.compare(integerLiteral2.floatValue(), integerLiteral1.floatValue());
  }


  private static boolean isInteger(IRI datatype) {
    return Objects.equals(datatype, XMLSchema.INT) || Objects.equals(datatype, XMLSchema.INTEGER);
  }

  private static boolean isDecimal(IRI datatype) {
    return Objects.equals(datatype, XMLSchema.FLOAT) || Objects.equals(datatype, XMLSchema.DOUBLE)
        || Objects.equals(datatype, XMLSchema.DECIMAL);
  }


}
