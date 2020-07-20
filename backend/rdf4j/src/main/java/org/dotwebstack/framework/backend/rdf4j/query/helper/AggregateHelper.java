package org.dotwebstack.framework.backend.rdf4j.query.helper;

import graphql.schema.GraphQLFieldDefinition;
import java.util.Optional;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.model.Aggregate;
import org.dotwebstack.framework.backend.rdf4j.query.model.AggregateType;
import org.dotwebstack.framework.core.input.CoreInputTypes;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class AggregateHelper {

  private AggregateHelper() {}

  public static Optional<Aggregate> resolveAggregate(GraphQLFieldDefinition fieldDefinition, Variable variable) {
    return Optional.ofNullable(fieldDefinition)
        .map(field -> field.getDirective(Rdf4jDirectives.AGGREGATE_NAME))
        .map(dir -> dir.getArgument(CoreInputTypes.AGGREGATE_TYPE))
        .map(argument -> argument.getValue()
            .toString())
        .map(AggregateType::valueOf)
        .map(aggregateType -> Aggregate.builder()
            .type(aggregateType)
            .variable(variable)
            .build());
  }
}
