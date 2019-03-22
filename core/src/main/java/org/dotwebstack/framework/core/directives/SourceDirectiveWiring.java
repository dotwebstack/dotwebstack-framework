package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.RequiredArgsConstructor;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.backend.Backend;
import org.dotwebstack.framework.core.backend.BackendRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SourceDirectiveWiring implements SchemaDirectiveWiring {

  private final BackendRegistry backendRegistry;

  @Override
  public GraphQLFieldDefinition onField(
      SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    String backendName = (String) environment.getDirective()
        .getArgument(Directives.SOURCE_ARG_BACKEND).getValue();

    if (!backendRegistry.has(backendName)) {
      throw new InvalidConfigurationException(
          String.format("Backend '%s' not found.", backendName));
    }

    Backend backend = backendRegistry.get(backendName);
    GraphQLFieldsContainer parentType = environment.getFieldsContainer();
    GraphQLFieldDefinition fieldDefinition = environment.getElement();

    environment.getCodeRegistry()
        .dataFetcher(parentType, fieldDefinition, backend.getObjectFetcher(fieldDefinition));

    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    if (!(outputType instanceof GraphQLObjectType)) {
      throw new InvalidConfigurationException("Output types other than object are not supported.");
    }

    GraphQLObjectType objectType = (GraphQLObjectType) outputType;

    objectType.getFieldDefinitions().forEach(
        childFieldDefinition -> environment.getCodeRegistry().dataFetcher(objectType,
            childFieldDefinition, backend.getPropertyFetcher(childFieldDefinition)));

    return fieldDefinition;
  }

}
