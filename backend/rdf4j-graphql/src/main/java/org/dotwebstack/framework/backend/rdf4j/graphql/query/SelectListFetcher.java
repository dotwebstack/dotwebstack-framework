package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import com.google.common.collect.ImmutableList;
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
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicateObjectList;

@Slf4j
@RequiredArgsConstructor
public final class SelectListFetcher implements DataFetcher<List<BindingSet>> {

  private final RepositoryConnection repositoryConnection;

  private final NodeShape nodeShape;

  @Override
  public List<BindingSet> get(@NonNull DataFetchingEnvironment environment) {
    SelectQuery selectQuery = Queries.SELECT();
    Variable subject = SparqlBuilder.var("subject");

    ImmutableList.Builder<RdfPredicateObjectList> reqPredObjBuilder = ImmutableList.builder();
    ImmutableList.Builder<RdfPredicateObjectList> optPredObjBuilder = ImmutableList.builder();

    reqPredObjBuilder
        .add(Rdf.predicateObjectList(Rdf.iri(RDF.TYPE), Rdf.iri(nodeShape.getTargetClass())));

    environment
        .getSelectionSet()
        .getFields()
        .stream()
        .map(field -> nodeShape.getPropertyShapes().get(field.getName()))
        .forEach(shape -> {
          ImmutableList.Builder<RdfPredicateObjectList> builder =
              shape.getMinCount() > 0 ? reqPredObjBuilder : optPredObjBuilder;
          builder.add(Rdf.predicateObjectList(Rdf.iri(shape.getPath()),
              SparqlBuilder.var(shape.getName())));
        });

    // Add a single triple pattern for required properties
    selectQuery.where(GraphPatterns
        .tp(subject, reqPredObjBuilder.build().toArray(new RdfPredicateObjectList[0])));

    // Add separate triple patterns for optional properties
    optPredObjBuilder.build()
        .stream()
        .map(predObjList -> (GraphPattern) GraphPatterns.tp(subject, predObjList))
        .forEach(triplePattern -> selectQuery.where(GraphPatterns.optional(triplePattern)));

    String selectQueryStr = selectQuery.getQueryString();
    LOG.debug("Exececuting query:\n{}", selectQueryStr);

    TupleQueryResult queryResult =
        repositoryConnection.prepareTupleQuery(selectQueryStr).evaluate();

    return QueryResults.asList(queryResult);
  }

}
