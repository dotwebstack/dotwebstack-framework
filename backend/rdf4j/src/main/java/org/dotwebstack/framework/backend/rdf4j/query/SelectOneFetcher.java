package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.dotwebstack.framework.backend.rdf4j.directives.Directives;
import org.dotwebstack.framework.backend.rdf4j.model.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.model.PropertyShape;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
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

  private final RepositoryConnection repositoryConnection;

  private final Model shapeModel;

  private final NodeShape nodeShape;

  public SelectOneFetcher(Repository repository, Model shapeModel, Resource nodeShape) {
    this.repositoryConnection = repository.getConnection();
    this.shapeModel = shapeModel;
    this.nodeShape = buildNodeShape(nodeShape);
  }

  @Override
  public BindingSet get(DataFetchingEnvironment environment) {
    SelectQuery selectQuery = Queries.SELECT();
    Resource subject = findSubject(environment);

    RdfPredicateObjectList[] predObjList = environment
        .getSelectionSet()
        .getFields()
        .stream()
        .map(field -> nodeShape.getPropertyShapes().get(field.getName()))
        .map(shape -> Rdf
            .predicateObjectList(Rdf.iri(shape.getPath()), SparqlBuilder.var(shape.getName())))
        .toArray(RdfPredicateObjectList[]::new);

    TriplePattern triplePattern = GraphPatterns.tp(Rdf.iri(subject.stringValue()), predObjList);

    String selectQueryStr = selectQuery
        .where(triplePattern)
        .getQueryString();

    LOG.debug("Exececuting query:\n{}", selectQueryStr);

    TupleQueryResult queryResult =
        repositoryConnection.prepareTupleQuery(selectQueryStr).evaluate();

    return QueryResults.asList(queryResult)
        .stream()
        .findFirst()
        .orElse(null);
  }

  private NodeShape buildNodeShape(Resource nodeShape) {
    return NodeShape.builder()
        .targetClass(ValueUtils.findIri(shapeModel, nodeShape, SHACL.TARGET_CLASS))
        .propertyShapes(buildPropertyShapes(nodeShape))
        .build();
  }

  private Map<String, PropertyShape> buildPropertyShapes(Resource nodeShape) {
    return Models
        .getPropertyResources(shapeModel, nodeShape, SHACL.PROPERTY)
        .stream()
        .map(shape -> PropertyShape.builder()
            .name(ValueUtils.findLiteral(shapeModel, shape, SHACL.NAME).stringValue())
            .path(ValueUtils.findIri(shapeModel, shape, SHACL.PATH))
            .minCount(ValueUtils.findLiteral(shapeModel, shape, SHACL.MIN_COUNT).intValue())
            .maxCount(ValueUtils.findLiteral(shapeModel, shape, SHACL.MAX_COUNT).intValue())
            .build())
        .collect(Collectors.toMap(PropertyShape::getName, Function.identity()));
  }

  private Resource findSubject(DataFetchingEnvironment environment) {
    return environment
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
  }

}
