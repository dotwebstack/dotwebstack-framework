package org.dotwebstack.framework.backend.rdf4j.query.model;

import graphql.schema.GraphQLObjectType;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;

@Builder
@Getter
public class FilterRule {
  private FieldPath fieldPath;

  private String operator;

  private Object value;

  private GraphQLObjectType objectType;

  public boolean isResource() {
    return fieldPath.isResource();
  }
}
