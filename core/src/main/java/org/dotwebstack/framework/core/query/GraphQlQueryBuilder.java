package org.dotwebstack.framework.core.query;

import lombok.NonNull;

public class GraphQlQueryBuilder {

  public String toQuery(@NonNull GraphQlField graphQlField) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("{");
    addToQuery(graphQlField, stringBuilder);
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  private void addToQuery(GraphQlField field, StringBuilder stringBuilder) {
    stringBuilder.append(field.getName());
    if (!field.getFields()
        .isEmpty()) {
      stringBuilder.append("{");
      String separator = "";
      for (GraphQlField childField : field.getFields()) {
        stringBuilder.append(separator);
        addToQuery(childField, stringBuilder);
        separator = ",";
      }
      stringBuilder.append("}");
    }
  }
}
