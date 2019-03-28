package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

@Slf4j
@RequiredArgsConstructor
public final class SelectListFetcher implements DataFetcher<List<BindingSet>> {

  private final RepositoryConnection repositoryConnection;

  private final NodeShape nodeShape;

  @Override
  public List<BindingSet> get(@NonNull DataFetchingEnvironment environment) {
    SelectQuery selectQuery = Queries.SELECT();
    Variable subject = SparqlBuilder.var("subject");

    selectQuery.where(GraphPatterns.tp(subject, RDF.TYPE, nodeShape.getTargetClass()));

    environment
        .getSelectionSet()
        .getFields()
        .stream()
        .map(field -> nodeShape.getPropertyShapes().get(field.getName()))
        .forEach(shape -> selectQuery.where(GraphPatterns.optional(
            GraphPatterns.tp(subject, shape.getPath(), SparqlBuilder.var(shape.getName())))));

    String selectQueryStr = selectQuery.getQueryString();
    LOG.debug("Exececuting query:\n{}", selectQueryStr);

    TupleQueryResult queryResult =
        repositoryConnection.prepareTupleQuery(selectQueryStr).evaluate();

    return QueryResults.asList(queryResult);
  }

}
