package org.dotwebstack.framework.backend.rdf4j.query;

import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.prefix;
import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.var;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.Directives;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicateObjectList;

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

    IRI subject = environment
        .getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argumentDefinition ->
            argumentDefinition.getDirective(Directives.SUBJECT_NAME) != null)
        .map(argumentDefinition -> {
          GraphQLDirective subjectDirective = argumentDefinition
              .getDirective(Directives.SUBJECT_NAME);
          String prefix = (String) subjectDirective
              .getArgument(Directives.SUBJECT_ARG_PREFIX)
              .getValue();
          String localName = environment.getArgument(argumentDefinition.getName());

          return vf.createIRI(prefix, localName);
        })
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException(
            "No type arguments with @subject directive found."));

    Prefix rdf = prefix(RDF.PREFIX, iri(RDF.NAMESPACE));
    Prefix bag = prefix("bag", iri("http://bag.basisregistraties.overheid.nl/def/bag#"));
    TriplePattern triplePattern = GraphPatterns.tp(subject, rdf.iri("type"), bag.iri("Pand"));

    RdfPredicateObjectList[] predicateObjectLists = environment
        .getSelectionSet()
        .getFields()
        .stream()
        .map(field -> Rdf.predicateObjectList(
            bag.iri(field.getName()), var(field.getName())))
        .toArray(RdfPredicateObjectList[]::new);

    String selectQueryStr = selectQuery
        .prefix(rdf, bag)
        .where(triplePattern.andHas(predicateObjectLists))
        .getQueryString();

    LOG.debug("Exececuting query:\n{}", selectQueryStr);

    TupleQueryResult queryResult =
        repositoryConnection.prepareTupleQuery(selectQueryStr).evaluate();

    return QueryResults.asList(queryResult)
        .stream()
        .findFirst()
        .orElse(null);
  }

}
