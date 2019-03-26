package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.graphql.model.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectOneFetcher;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.graphql.directives.DirectiveUtils;
import org.springframework.stereotype.Component;

@Component
public class SelectDirectiveWiring extends AbstractDirectiveWiring {

  SelectDirectiveWiring(BackendRegistry backendRegistry) {
    super(backendRegistry);
  }

  @Override
  public GraphQLFieldDefinition onField(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLFieldsContainer parentType = environment.getFieldsContainer();

    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    if (!(outputType instanceof GraphQLObjectType)) {
      throw new InvalidConfigurationException(
          "Query output types other than objects are not yet supported.");
    }

    GraphQLObjectType objectType = (GraphQLObjectType) outputType;
    NodeShape nodeShape = getNodeShape(objectType);

    Rdf4jBackend selectBackend = getBackend(DirectiveUtils
        .getStringArgument(environment.getDirective(), Directives.SELECT_ARG_BACKEND));

    String subjectTemplate = (String) environment.getDirective()
        .getArgument(Directives.SELECT_ARG_SUBJECT).getValue();
    DataFetcher objectFetcher = new SelectOneFetcher(selectBackend.getRepository().getConnection(),
        nodeShape, subjectTemplate);
    environment.getCodeRegistry().dataFetcher(parentType, fieldDefinition, objectFetcher);

    DataFetcher scalarFetcher = new BindingSetFetcher();
    objectType.getFieldDefinitions()
        .stream()
        .filter(childFieldDefinition -> GraphQLTypeUtil
            .unwrapNonNull(childFieldDefinition.getType()) instanceof GraphQLScalarType)
        .forEach(childFieldDefinition -> environment.getCodeRegistry()
            .dataFetcher(objectType, childFieldDefinition, scalarFetcher));

    return fieldDefinition;
  }

}
