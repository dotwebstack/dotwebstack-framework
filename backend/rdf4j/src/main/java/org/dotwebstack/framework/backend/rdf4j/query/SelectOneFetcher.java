package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;
import graphql.schema.SelectedField;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jDirectives;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

@Slf4j
@RequiredArgsConstructor
public final class SelectOneFetcher implements DataFetcher<BindingSet> {

  private static final ValueFactory vf = SimpleValueFactory.getInstance();

  private static final String SUBJECT_VARIABLE_NAME = "subject";

  private final RepositoryConnection repositoryConnection;

  public SelectOneFetcher(Repository repository) {
    this.repositoryConnection = repository.getConnection();
  }

  @Override
  public BindingSet get(DataFetchingEnvironment environment) {
    SelectQuery selectQuery = Queries.SELECT();
    Variable subjectVar = SparqlBuilder.var(SUBJECT_VARIABLE_NAME);

    Optional<IRI> subjectIri = environment
        .getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argumentDefinition ->
            argumentDefinition.getDirective(Rdf4jDirectives.SUBJECT_NAME) != null)
        .map(argumentDefinition -> {
          GraphQLDirective subjectDirective = argumentDefinition
              .getDirective(Rdf4jDirectives.SUBJECT_NAME);
          String prefix = (String) subjectDirective
              .getArgument(Rdf4jDirectives.SUBJECT_ARG_PREFIX)
              .getValue();
          String localName = environment.getArgument(argumentDefinition.getName());

          return vf.createIRI(prefix, localName);
        })
        .findFirst();

    environment
        .getSelectionSet()
        .getFields()
        .stream()
        .map(SelectedField::getName)
        .forEach(field -> {
          IRI predicateIri = vf
              .createIRI("http://bag.basisregistraties.overheid.nl/def/bag#", field);

          if (subjectIri.isPresent()) {
            selectQuery
                .where(GraphPatterns.tp(subjectIri.get(), predicateIri, SparqlBuilder.var(field)));
          } else {
            selectQuery.where(GraphPatterns.tp(subjectVar, predicateIri, SparqlBuilder.var(field)));
          }
        });

    String selectQueryStr = selectQuery.getQueryString();
    LOG.debug("Exececuting query: {}", selectQueryStr);

    TupleQueryResult queryResult =
        repositoryConnection.prepareTupleQuery(selectQueryStr).evaluate();

    return QueryResults.asList(queryResult).stream().findFirst().orElse(null);
  }

}
