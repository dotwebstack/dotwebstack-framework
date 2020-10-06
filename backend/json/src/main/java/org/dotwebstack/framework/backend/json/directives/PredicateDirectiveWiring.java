package org.dotwebstack.framework.backend.json.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class PredicateDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  @Override
  public String getDirectiveName() {
    return PredicateDirectives.PREDICATE_NAME;
  }

  @Override
  public GraphQLArgument onArgument(@NonNull SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    return environment.getElement();
  }
}
