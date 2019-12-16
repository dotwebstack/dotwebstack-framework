package org.dotwebstack.framework.backend.rdf4j.query.context;

import graphql.schema.GraphQLArgument;
import graphql.schema.SelectedField;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.query.FieldPath;

@Builder
@Getter
public class ArgumentResultWrapper {
  private GraphQLArgument argument;

  private SelectedField selectedField;

  private FieldPath fieldPath;
}
