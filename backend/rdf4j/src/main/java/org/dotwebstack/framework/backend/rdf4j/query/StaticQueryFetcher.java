package org.dotwebstack.framework.backend.rdf4j.query;

import static java.util.Collections.emptyList;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.graphQlFieldDefinitionIsOfType;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.converters.Rdf4jConverterRouter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.validators.QueryValidator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

@Slf4j
public final class StaticQueryFetcher implements DataFetcher<Object> {

  private static final List<GraphQLScalarType> SUPPORTED_TYPE_NAMES =
      Arrays.asList(Rdf4jScalars.IRI, Rdf4jScalars.MODEL);

  private final RepositoryAdapter repositoryAdapter;

  private final List<QueryValidator> validators;

  private final Rdf4jConverterRouter converterRouter;

  private String staticSparqlQuery;

  public StaticQueryFetcher(RepositoryAdapter repositoryAdapter, List<QueryValidator> validators,
      Rdf4jConverterRouter converterRouter, @NonNull String staticSparqlQuery) {
    this.repositoryAdapter = repositoryAdapter;
    this.validators = validators;
    this.converterRouter = converterRouter;
    this.staticSparqlQuery = staticSparqlQuery;
  }

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {

    validators.forEach(validator -> validator.validate(environment));
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

    if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.IRI)) {
      return getQueryResultAsIri(environment, fieldDefinition);
    }

    if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.MODEL)) {
      return getQueryResultAsModel(environment, fieldDefinition);
    }

    throw invalidConfigurationException("Only Model and IRI graphQL outputTypes are supported");
  }

  private Model getQueryResultAsModel(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition) {
    GraphQueryResult graphQueryResult = fetchGraphQuery(environment, fieldDefinition, staticSparqlQuery);

    Model model = QueryResults.asModel(graphQueryResult);
    LOG.debug("Fetched [{}] triples", model.size());

    return model;
  }

  private Value getQueryResultAsIri(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition) {
    TupleQueryResult tupleQueryResult = fetchTupleQuery(environment, fieldDefinition, staticSparqlQuery);

    BindingSet queryBindingSet = tupleQueryResult.next();

    validateQueryHasOneResult(tupleQueryResult, staticSparqlQuery);

    return queryBindingSet.getValue(fieldDefinition.getName());
  }

  private void validateQueryHasOneResult(TupleQueryResult tupleQueryResult, String query) {
    if (tupleQueryResult.hasNext()) {
      throw illegalStateException("Query result for query: {} has more than 1 result", query);
    }
  }

  public static boolean supports(GraphQLFieldDefinition fieldDefinition) {
    String inputTypeName = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType())
        .getName();

    return SUPPORTED_TYPE_NAMES.stream()
        .map(GraphQLScalarType::getName)
        .anyMatch(inputTypeName::equals);
  }

  private String getRepositoryId(GraphQLFieldDefinition fieldDefinition) {
    GraphQLDirective sparqlDirective = fieldDefinition.getDirective(Rdf4jDirectives.SPARQL_NAME);

    return DirectiveUtils.getArgument(sparqlDirective, Rdf4jDirectives.SPARQL_ARG_REPOSITORY, String.class);
  }

  private GraphQueryResult fetchGraphQuery(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition,
      String query) {
    LOG.debug("Executing query for graph: {}", query);

    GraphQuery queryResult =
        repositoryAdapter.prepareGraphQuery(getRepositoryId(fieldDefinition), environment, query, emptyList());

    bindValues(environment, fieldDefinition, queryResult);

    return queryResult.evaluate();
  }

  private TupleQueryResult fetchTupleQuery(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition,
      String query) {
    LOG.debug("Executing query for tuples: {}", query);

    TupleQuery queryResult = repositoryAdapter.prepareTupleQuery(getRepositoryId(fieldDefinition), environment, query);

    bindValues(environment, fieldDefinition, queryResult);

    return queryResult.evaluate();
  }

  private void bindValues(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition, Query query) {
    Map<String, Object> queryParameters = environment.getArguments();
    List<GraphQLArgument> definedArguments = fieldDefinition.getArguments();

    queryParameters.forEach((key, value) -> {

      GraphQLArgument graphQlArgument = definedArguments.stream()
          .filter(argument -> argument.getName()
              .equals(key))
          .findFirst()
          .orElseThrow(() -> illegalStateException("Invoked endpoint does not support argument {}", key));

      Value bindingValue = getValueForType(value, GraphQLTypeUtil.unwrapAll(graphQlArgument.getType())
          .getName());

      query.setBinding(key, bindingValue);

    });
  }

  private Value getValueForType(Object value, String typeAsString) {
    return converterRouter.convertToValue(value, typeAsString);
  }

}
