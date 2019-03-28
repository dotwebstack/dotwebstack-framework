package org.dotwebstack.framework.backend.rdf4j.graphql.directives;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jBackend;
import org.dotwebstack.framework.backend.rdf4j.graphql.GraphqlObjectShapeRegistry;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectListFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectOneFetcher;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.graphql.directives.DirectiveUtils;
import org.springframework.stereotype.Component;

@Component
public class SparqlDirectiveWiring extends AbstractDirectiveWiring {

  private final GraphqlObjectShapeRegistry objectShapeRegistry;

  SparqlDirectiveWiring(BackendRegistry backendRegistry,
      GraphqlObjectShapeRegistry objectShapeRegistry) {
    super(backendRegistry);
    this.objectShapeRegistry = objectShapeRegistry;
  }

  @Override
  public GraphQLFieldDefinition onField(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLFieldsContainer parentType = environment.getFieldsContainer();

    Rdf4jBackend backend = getBackend(DirectiveUtils
        .getStringArgument(Directives.SPARQL_ARG_BACKEND, environment.getDirective()));
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    if (outputType instanceof GraphQLObjectType) {
      GraphQLObjectType objectType = (GraphQLObjectType) outputType;
      String subjectTemplate = (String) environment.getDirective()
          .getArgument(Directives.SPARQL_ARG_SUBJECT).getValue();
      SelectOneFetcher dataFetcher = new SelectOneFetcher(backend.getRepository().getConnection(),
          objectShapeRegistry.get(objectType), subjectTemplate);
      environment.getCodeRegistry().dataFetcher(parentType, fieldDefinition, dataFetcher);
      configureFields(objectType, environment.getCodeRegistry());

      return fieldDefinition;
    }

    if (outputType instanceof GraphQLList) {
      GraphQLUnmodifiedType listType = GraphQLTypeUtil.unwrapAll(outputType);

      if (listType instanceof GraphQLObjectType) {
        GraphQLObjectType objectType = (GraphQLObjectType) listType;
        SelectListFetcher dataFetcher = new SelectListFetcher(
            backend.getRepository().getConnection(), objectShapeRegistry.get(objectType));
        environment.getCodeRegistry().dataFetcher(parentType, fieldDefinition, dataFetcher);
        configureFields(objectType, environment.getCodeRegistry());

        return fieldDefinition;
      }
    }

    throw new InvalidConfigurationException(
        "Query output types other than objects or lists are not yet supported.");
  }

  private void configureFields(GraphQLObjectType objectType,
      GraphQLCodeRegistry.Builder codeRegistry) {
    DataFetcher scalarFetcher = new BindingSetFetcher();

    objectType.getFieldDefinitions()
        .stream()
        .filter(childFieldDefinition -> GraphQLTypeUtil
            .unwrapNonNull(childFieldDefinition.getType()) instanceof GraphQLScalarType)
        .forEach(childFieldDefinition -> codeRegistry
            .dataFetcher(objectType, childFieldDefinition, scalarFetcher));
  }

}
