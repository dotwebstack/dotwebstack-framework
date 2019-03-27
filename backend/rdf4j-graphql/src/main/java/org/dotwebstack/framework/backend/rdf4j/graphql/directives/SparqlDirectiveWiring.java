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
import org.dotwebstack.framework.backend.rdf4j.graphql.query.BindingSetFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectListFetcher;
import org.dotwebstack.framework.backend.rdf4j.graphql.query.SelectOneFetcher;
import org.dotwebstack.framework.core.BackendRegistry;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.springframework.stereotype.Component;

@Component
public class SparqlDirectiveWiring extends AbstractDirectiveWiring {

  SparqlDirectiveWiring(BackendRegistry backendRegistry) {
    super(backendRegistry);
  }

  @Override
  public GraphQLFieldDefinition onField(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    GraphQLFieldsContainer parentType = environment.getFieldsContainer();

    GraphQLObjectType queryType;
    if (parentType.getName().equals("Query") && parentType instanceof GraphQLObjectType) {
      queryType = (GraphQLObjectType) parentType;
    } else {
      throw new InvalidConfigurationException("Query type could not be found.");
    }

    Rdf4jBackend backend = getBackend(getInheritableArgument(Directives.SPARQL_ARG_BACKEND,
        environment.getDirective(), queryType));
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    if (outputType instanceof GraphQLObjectType) {
      GraphQLObjectType objectType = (GraphQLObjectType) outputType;
      String subjectTemplate = (String) environment.getDirective()
          .getArgument(Directives.SPARQL_ARG_SUBJECT).getValue();
      SelectOneFetcher dataFetcher = new SelectOneFetcher(backend.getRepository().getConnection(),
          getNodeShape(objectType, queryType), subjectTemplate);
      environment.getCodeRegistry().dataFetcher(parentType, fieldDefinition, dataFetcher);
      configureFields(objectType, environment.getCodeRegistry());

      return fieldDefinition;
    }

    if (outputType instanceof GraphQLList) {
      GraphQLUnmodifiedType listType = GraphQLTypeUtil.unwrapAll(outputType);

      if (listType instanceof GraphQLObjectType) {
        GraphQLObjectType objectType = (GraphQLObjectType) listType;
        SelectListFetcher dataFetcher = new SelectListFetcher(
            backend.getRepository().getConnection(), getNodeShape(objectType, queryType));
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
