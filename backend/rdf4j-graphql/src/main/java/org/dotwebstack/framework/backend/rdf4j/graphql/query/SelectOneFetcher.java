package org.dotwebstack.framework.backend.rdf4j.graphql.query;

import com.google.common.collect.ImmutableList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicateObjectList;

@Slf4j
@RequiredArgsConstructor
public final class SelectOneFetcher implements DataFetcher<BindingSet> {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private final RepositoryConnection repositoryConnection;

  private final NodeShape nodeShape;

  private final String subjectTemplate;

  @Override
  public BindingSet get(@NonNull DataFetchingEnvironment environment) {
    SelectQuery selectQuery = Queries.SELECT();
    Resource subject = findSubject(environment);

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
    selectQuery.where(GraphPatterns.tp(Rdf.iri(subject.stringValue()),
        reqPredObjBuilder.build().toArray(new RdfPredicateObjectList[0])));

    // Add separate triple patterns for optional properties
    optPredObjBuilder.build()
        .stream()
        .map(predObjList -> (GraphPattern) GraphPatterns
            .tp(Rdf.iri(subject.stringValue()), predObjList))
        .forEach(triplePattern ->
            selectQuery.where(GraphPatterns.optional(triplePattern)));

    String selectQueryStr = selectQuery.getQueryString();
    LOG.debug("Exececuting query:\n{}", selectQueryStr);

    TupleQueryResult queryResult =
        repositoryConnection.prepareTupleQuery(selectQueryStr).evaluate();

    return QueryResults.asList(queryResult)
        .stream()
        .findFirst()
        .orElse(null);
  }

  private Resource findSubject(DataFetchingEnvironment environment) {
    Pattern regex = Pattern.compile("\\$\\{(\\w+)}");
    Matcher regexMatcher = regex.matcher(subjectTemplate);
    StringBuffer stringBuffer = new StringBuffer();

    while (regexMatcher.find()) {
      String argName = regexMatcher.group(1);
      String argValue = environment.getArgument(argName);
      regexMatcher.appendReplacement(stringBuffer, argValue);
    }

    regexMatcher.appendTail(stringBuffer);

    return VF.createIRI(stringBuffer.toString());
  }

}
