package org.dotwebstack.framework.backend.rdf4j.query.context;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.SelectedField;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ArgumentResultWrapper {
  private GraphQLArgument argument;

  private SelectedField selectedField;

  private List<GraphQLFieldDefinition> fieldPath;
}
