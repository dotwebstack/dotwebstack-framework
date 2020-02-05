package org.dotwebstack.framework.backend.rdf4j.query;

import static java.util.Collections.emptyList;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQLFieldDefinitionHelper.graphQlFieldDefinitionIsOfType;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.validators.QueryValidator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;

@Slf4j
public final class StaticQueryFetcher implements DataFetcher<Object> {

  private final RepositoryAdapter repositoryAdapter;

  private final List<QueryValidator> validators;

  private String staticSparqlQuery;

  public StaticQueryFetcher(RepositoryAdapter repositoryAdapter, List<QueryValidator> validators,
      String staticSparqlQuery) {
    this.repositoryAdapter = repositoryAdapter;
    this.validators = validators;
    this.staticSparqlQuery = staticSparqlQuery;
  }

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {

    validators.forEach(validator -> validator.validate(environment));
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

    if (Objects.nonNull(staticSparqlQuery)) {

      if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.IRI)) {
        return getQueryResultAsIri(environment, fieldDefinition);

      } else if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.MODEL)) {
        return getQueryResultAsModel(environment, fieldDefinition);

      } else {
        throw invalidConfigurationException("Only Model and IRI graphQL outputTypes are supported");
      }
    }

    return fieldDefinition.getArguments()
        .stream()
        .filter(arg -> Objects.nonNull(arg.getDirective(Rdf4jDirectives.RESOURCE_NAME)))
        .map(GraphQLArgument::getName)
        .map(environment::getArgument)
        .map(Object::toString)
        .findFirst()
        .map(subject -> String.format("describe <%s>", subject))
        .map(query -> fetchGraphQuery(environment, fieldDefinition, query))
        .map(this::mapQueryResultToModel)
        .orElseGet(TreeModel::new);
  }

  private Object getQueryResultAsModel(@NonNull DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition) {
    GraphQueryResult graphQueryResult = fetchGraphQuery(environment, fieldDefinition, staticSparqlQuery);

    Model model = mapQueryResultToModel(graphQueryResult);

    if (model.isEmpty()) {
      return null;
    }

    return model;
  }

  private Object getQueryResultAsIri(@NonNull DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition) {
    TupleQueryResult tupleQueryResult = fetchTupleQuery(environment, fieldDefinition, staticSparqlQuery);

    BindingSet queryBindingSet = tupleQueryResult.next();

    validateQueryHasOneResult(tupleQueryResult, staticSparqlQuery);

    String bindingName = queryBindingSet.getBindingNames()
        .stream()
        .findFirst()
        .get();

    return queryBindingSet.getValue(bindingName);
  }

  private void validateQueryHasOneResult(TupleQueryResult tupleQueryResult, String query) {
    if (tupleQueryResult.hasNext()) {
      throw illegalStateException("Query result for query: {} has more than 1 result", query);
    }
  }

  public static boolean supports(GraphQLFieldDefinition fieldDefinition) {
    String inputTypeName = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType())
        .getName();

    return Stream.of(Rdf4jScalars.IRI, Rdf4jScalars.MODEL)
        .map(GraphQLScalarType::getName)
        .anyMatch(inputTypeName::equals);
  }

  private String getRepositoryId(GraphQLFieldDefinition fieldDefinition) {
    GraphQLDirective sparqlDirective = fieldDefinition.getDirective(Rdf4jDirectives.SPARQL_NAME);

    return DirectiveUtils.getArgument(sparqlDirective, Rdf4jDirectives.SPARQL_ARG_REPOSITORY, String.class);
  }

  private GraphQueryResult fetchGraphQuery(@NonNull DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition, String query) {
    LOG.debug("Executing query for graph: {}", query);
    return repositoryAdapter.prepareGraphQuery(getRepositoryId(fieldDefinition), environment, query, emptyList())
        .evaluate();
  }

  private TupleQueryResult fetchTupleQuery(@NonNull DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition, String query) {
    LOG.debug("Executing query for tuples: {}", query);
    return repositoryAdapter.prepareTupleQuery(getRepositoryId(fieldDefinition), environment, query)
        .evaluate();

  }

  private Model mapQueryResultToModel(GraphQueryResult queryResult) {
    Model model = QueryResults.asModel(queryResult);
    LOG.debug("Fetched [{}] triples", model.size());
    return model;
  }

}
