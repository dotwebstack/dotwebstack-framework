package org.dotwebstack.framework.core.query;

import java.util.StringJoiner;
import lombok.NonNull;

public class GraphQlQueryBuilder {

  public String toQuery(@NonNull GraphQlField graphQlField) {
    StringJoiner joiner = new StringJoiner(",", "{", "}");
    addToQuery(graphQlField, joiner);
    return joiner.toString();
  }

  private void addToQuery(GraphQlField field, StringJoiner joiner) {
    if (!field.getFields()
        .isEmpty()) {
      StringJoiner childJoiner = new StringJoiner(",", "{", "}");
      field.getFields()
          .forEach(childField -> addToQuery(childField, childJoiner));
      joiner.add(field.getName() + childJoiner.toString());
    } else {
      joiner.add(field.getName());
    }
  }
}
