package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeString;

import java.util.Map;
import java.util.StringJoiner;
import lombok.NonNull;

public class GraphQlQueryBuilder {

  public String toQuery(@NonNull GraphQlField graphQlField, Map<String, Object> inputParams) {
    StringBuilder builder = new StringBuilder();
    StringJoiner joiner = new StringJoiner(",", "{", "}");
    StringJoiner argumentJoiner = new StringJoiner(",");

    addToQuery(graphQlField, joiner, argumentJoiner, inputParams, true);
    builder.append("query Wrapper");
    if (!argumentJoiner.toString()
        .isEmpty()) {
      builder.append("(");
      builder.append(argumentJoiner);
      builder.append(")");
    }
    builder.append(joiner.toString());
    return builder.toString();
  }

  private void addToQuery(GraphQlField field, StringJoiner joiner, StringJoiner headerArgumentJoiner,
      Map<String, Object> inputParams, boolean isTopLevel) {
    StringJoiner argumentJoiner = new StringJoiner(",", "(", ")");
    argumentJoiner.setEmptyValue("");
    if (!field.getArguments()
        .isEmpty() && isTopLevel) {
      field.getArguments()
          .stream()
          .filter(graphQlArgument -> inputParams.containsKey(graphQlArgument.getName()))
          .forEach(graphQlArgument -> {
            argumentJoiner.add(graphQlArgument.getName() + ": $" + graphQlArgument.getName());
            headerArgumentJoiner.add("$" + graphQlArgument.getName() + ": " + getTypeString(graphQlArgument.getType()));
          });
    }
    if (!field.getFields()
        .isEmpty()) {
      StringJoiner childJoiner = new StringJoiner(",", "{", "}");
      field.getFields()
          .forEach(childField -> addToQuery(childField, childJoiner, headerArgumentJoiner, inputParams, false));
      joiner.add(field.getName() + argumentJoiner.toString() + childJoiner.toString());
    } else {
      joiner.add(field.getName() + argumentJoiner.toString());
    }
  }
}
