package org.dotwebstack.framework.backend.rdf4j.query;

import static java.util.Collections.emptyList;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.directives.DirectiveUtils;
import org.dotwebstack.framework.core.validators.QueryValidator;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;

@Slf4j
public final class FixedQueryFetcher implements DataFetcher<Object> {

  private static final String GRAPH = "graph";

  private final RepositoryAdapter repositoryAdapter;

  private final List<QueryValidator> validators;

  public FixedQueryFetcher(RepositoryAdapter repositoryAdapter, List<QueryValidator> validators) {
    this.repositoryAdapter = repositoryAdapter;
    this.validators = validators;
  }

  @Override
  public Object get(@NonNull DataFetchingEnvironment environment) {
    isSupported(environment.getFieldType());

    validators.forEach(validator -> validator.validate(environment));
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

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

  private void isSupported(GraphQLOutputType fieldType) {
    if (!GraphQLTypeUtil.unwrapNonNull(fieldType)
        .getName()
        .equals(Rdf4jScalars.MODEL.getName())) {
      throw new UnsupportedOperationException("Field output types other than Model aren't supported for fixed queries");
    }
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

  private Model mapQueryResultToModel(GraphQueryResult queryResult) {
    Model model = QueryResults.asModel(queryResult);
    LOG.debug("Fetched [{}] triples", model.size());
    return model;
  }

}
