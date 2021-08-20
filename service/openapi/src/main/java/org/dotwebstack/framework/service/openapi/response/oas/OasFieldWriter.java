package org.dotwebstack.framework.service.openapi.response.oas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OasFieldWriter {

  public static String toString(OasField field) {
    StringBuilder sb = new StringBuilder();
    int indent = 0;
    List<OasField> stack = List.of();
    toString(field, sb, indent, stack);
    return sb.toString();
  }

  private static void toString(OasField field, StringBuilder sb, int indent, List<OasField> stack) {
    if (stack.stream()
        .filter(s -> s.equals(field))
        .count() >= 2) {
      return;
    }

    switch (field.getType()) {
      case ARRAY:
        sb.append("[");
        toString(((OasArrayField) field).getContent(), sb, indent, stack);
        sb.append("]");
        break;
      case OBJECT:
        OasObjectField of = (OasObjectField) field;
        String prefix = "";
        sb.append("{\n");
        for (Map.Entry<String, OasField> e : of.getFields()
            .entrySet()) {
          sb.append(prefix);
          indent(sb, indent + 2);
          sb.append(e.getKey());
          sb.append(": ");
          toString(e.getValue(), sb, indent + 2, addToStack(field, stack));
          if (e.getValue()
              .isRequired()) {
            sb.append("!");
          }
          prefix = ",\n";
        }
        sb.append("\n");
        indent(sb, indent);
        sb.append("}");
        break;
      case SCALAR:
        sb.append(((OasScalarField) field).getScalarType());
        break;
      case SCALAR_EXPRESSION:
        sb.append(((OasScalarExpressionField) field).getScalarType());
        break;
      case ONE_OF:
        sb.append("[OneOf]");
        sb.append(((OasOneOfField) field).getContent()
            .stream()
            .map(OasField::getType)
            .collect(Collectors.toList()));
        break;
      default:
        break;
    }
  }

  private static List<OasField> addToStack(OasField field, List<OasField> stack) {
    List<OasField> result = new ArrayList<>(stack);
    result.add(field);
    return result;
  }

  private static void indent(StringBuilder sb, int indent) {
    IntStream.range(0, indent)
        .forEach(i -> sb.append(' '));
  }
}
