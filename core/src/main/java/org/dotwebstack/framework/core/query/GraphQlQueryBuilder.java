package org.dotwebstack.framework.core.query;

import java.util.Map;
import java.util.StringJoiner;
import lombok.NonNull;

public class GraphQlQueryBuilder {

  public String toQuery(@NonNull GraphQlField graphQlField, Map<String, String> inputParams) {
    StringJoiner joiner = new StringJoiner(",", "{", "}");
    addToQuery(graphQlField, joiner, inputParams);
    return joiner.toString();
  }

  private void addToQuery(GraphQlField field, StringJoiner joiner, Map<String, String> inputParams) {
    StringJoiner argumentJoiner = new StringJoiner(",", "(", ")");
    argumentJoiner.setEmptyValue("");
    if (!field.getArguments()
        .isEmpty()) {
      field.getArguments()
          .stream()
          .filter(a -> inputParams.containsKey(a.getName()))
          .forEach(a -> argumentJoiner.add(a.getName() + ": " + inputParams.get(a.getName())));
    }
    if (!field.getFields()
        .isEmpty()) {
      StringJoiner childJoiner = new StringJoiner(",", "{", "}");
      field.getFields()
          .forEach(childField -> addToQuery(childField, childJoiner, inputParams));
      joiner.add(field.getName() + argumentJoiner.toString() + childJoiner.toString());
    } else {
      joiner.add(field.getName() + argumentJoiner.toString());
    }
  }
}
