package org.dotwebstack.framework.backend.rdf4j.query;

import static java.util.Collections.emptyList;
import static org.dotwebstack.framework.backend.rdf4j.helper.GraphQlFieldDefinitionHelper.graphQlFieldDefinitionIsOfType;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.converters.Rdf4jConverterRouter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlQueryResult;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.validators.QueryValidator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;

@Slf4j
public final class StaticQueryFetcher implements DataFetcher<Object> {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final String SUBJECT = "subject";

  private static final List<GraphQLScalarType> SUPPORTED_TYPE_NAMES =
      Arrays.asList(Rdf4jScalars.IRI, Rdf4jScalars.MODEL, Rdf4jScalars.SPARQL_QUERY_RESULT);

  private final RepositoryAdapter repositoryAdapter;

  private final List<QueryValidator> validators;

  private final Rdf4jConverterRouter converterRouter;

  private final String staticSparqlQuery;

  private JexlHelper jexlHelper;

  public StaticQueryFetcher(RepositoryAdapter repositoryAdapter, List<QueryValidator> validators,
      Rdf4jConverterRouter converterRouter, @NonNull String staticSparqlQuery, @NonNull JexlHelper jexlHelper) {
    this.repositoryAdapter = repositoryAdapter;
    this.validators = validators;
    this.converterRouter = converterRouter;
    this.staticSparqlQuery = staticSparqlQuery;
    this.jexlHelper = jexlHelper;
  }

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {

    validators.forEach(validator -> validator.validate(environment));
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

    if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.IRI)) {
      return getQueryResultAsIri(environment, fieldDefinition).orElse(null);
    }

    if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.MODEL)) {
      return getQueryResultAsModel(environment, fieldDefinition).orElse(null);
    }

    if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.SPARQL_QUERY_RESULT)) {
      return getQueryResultAsSparqlResult(environment, fieldDefinition);
    }

    throw invalidConfigurationException("Only Model and IRI graphQL outputTypes are supported");
  }

  private Optional<Model> getQueryResultAsModel(DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition) {
    GraphQueryResult graphQueryResult = fetchGraphQuery(environment, fieldDefinition, staticSparqlQuery);

    Model model = QueryResults.asModel(graphQueryResult);

    if (model.isEmpty()) {
      return Optional.empty();
    }

    LOG.debug("Fetched [{}] triples", model.size());

    return Optional.of(model);
  }

  private SparqlQueryResult getQueryResultAsSparqlResult(DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition) {
    LOG.debug("Executing query for sparql: {}", staticSparqlQuery);

    TupleQueryResult tupleQueryResult = fetchTupleQuery(environment, fieldDefinition, staticSparqlQuery);

    if (tupleQueryResult.hasNext()) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      SPARQLResultsXMLWriter writer = new SPARQLResultsXMLWriter(outputStream);
      QueryResults.report(tupleQueryResult, writer);
      return new SparqlQueryResult(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    return new SparqlQueryResult();
  }

  private Optional<Value> getQueryResultAsIri(DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition) {
    TupleQueryResult tupleQueryResult = fetchTupleQuery(environment, fieldDefinition, staticSparqlQuery);

    if (!tupleQueryResult.hasNext()) {
      return Optional.empty();
    }

    BindingSet queryBindingSet = tupleQueryResult.next();

    validateQueryHasOneResult(tupleQueryResult, staticSparqlQuery);

    return Optional.ofNullable(queryBindingSet.getValue(fieldDefinition.getName()));
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

    GraphQuery graphQuery =
        repositoryAdapter.prepareGraphQuery(getRepositoryId(fieldDefinition), environment, query, emptyList());

    bindValues(environment, fieldDefinition, graphQuery);

    return graphQuery.evaluate();
  }

  private TupleQueryResult fetchTupleQuery(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition,
      String query) {
    LOG.debug("Executing query for tuples: {}", query);

    TupleQuery tupleQuery = repositoryAdapter.prepareTupleQuery(getRepositoryId(fieldDefinition), environment, query);

    bindValues(environment, fieldDefinition, tupleQuery);

    return tupleQuery.evaluate();
  }

  private void bindValues(DataFetchingEnvironment environment, GraphQLFieldDefinition fieldDefinition, Query query) {
    getBindingValues(environment, fieldDefinition).forEach(query::setBinding);
  }

  private Map<String, Value> getBindingValues(DataFetchingEnvironment environment,
      GraphQLFieldDefinition fieldDefinition) {
    GraphQLDirective sparqlDirective = fieldDefinition.getDirective(Rdf4jDirectives.SPARQL_NAME);
    Map<String, Object> queryParameters = environment.getArguments();
    List<GraphQLArgument> definedArguments = fieldDefinition.getArguments();

    String subjectTemplate =
        DirectiveUtils.getArgument(sparqlDirective, Rdf4jDirectives.SPARQL_ARG_SUBJECT, String.class);
    if (subjectTemplate != null) {
      JexlContext jexlContext = JexlHelper.getJexlContext(fieldDefinition);
      queryParameters.forEach(jexlContext::set);
      String evaluatedSubject = this.jexlHelper.evaluateScript(subjectTemplate, jexlContext, String.class)
          .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
              "Directive {} argument {} returned an " + "empty response for JEXL expression {}",
              sparqlDirective.getName(), Rdf4jDirectives.SPARQL_ARG_SUBJECT, subjectTemplate));
      return ImmutableMap.of(SUBJECT, VF.createIRI(evaluatedSubject));
    } else {
      Map<String, Value> result = new HashMap<>();
      queryParameters.forEach((key, value) -> {

        GraphQLArgument graphQlArgument = definedArguments.stream()
            .filter(argument -> argument.getName()
                .equals(key))
            .findFirst()
            .orElseThrow(() -> illegalStateException("Invoked endpoint does not support argument {}", key));

        Value bindingValue = getValueForType(value, GraphQLTypeUtil.unwrapAll(graphQlArgument.getType())
            .getName());


        String keyPrefix = "";
        if (graphQlFieldDefinitionIsOfType(fieldDefinition.getType(), Rdf4jScalars.SPARQL_QUERY_RESULT)) {
          keyPrefix = "arg_";
        }

        result.put(keyPrefix + key, bindingValue);
      });
      return result;
    }
  }

  private Value getValueForType(Object value, String typeAsString) {
    return converterRouter.convertToValue(value, typeAsString);
  }

}
