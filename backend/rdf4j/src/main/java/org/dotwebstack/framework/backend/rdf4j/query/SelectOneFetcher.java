package org.dotwebstack.framework.backend.rdf4j.query;

import com.google.common.collect.ImmutableList;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLDirective;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.rdf4j.directives.Directives;
import org.dotwebstack.framework.backend.rdf4j.model.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.model.PropertyShape;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
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
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicateObjectList;

@Slf4j
@RequiredArgsConstructor
public final class SelectOneFetcher implements DataFetcher<BindingSet> {

  private static final ValueFactory vf = SimpleValueFactory.getInstance();

  private final RepositoryConnection repositoryConnection;

  private final Model shapeModel;

  private final NodeShape nodeShape;

  public SelectOneFetcher(@NonNull Repository repository, @NonNull Model shapeModel,
      @NonNull Resource nodeShape) {
    this.repositoryConnection = repository.getConnection();
    this.shapeModel = shapeModel;
    this.nodeShape = buildNodeShape(nodeShape);
  }

  @Override
  public BindingSet get(@NonNull DataFetchingEnvironment environment) {
    SelectQuery selectQuery = Queries.SELECT();
    Resource subject = findSubject(environment);

    ImmutableList.Builder<RdfPredicateObjectList> reqPredObjBuilder = ImmutableList.builder();
    ImmutableList.Builder<RdfPredicateObjectList> optPredObjBuilder = ImmutableList.builder();

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
    GraphPattern pattern = GraphPatterns
        .tp(Rdf.iri(subject.stringValue()), reqPredObjBuilder.build()
            .toArray(new RdfPredicateObjectList[0]));

    // Add separate triple patterns for optional properties
    pattern = optPredObjBuilder.build()
        .stream()
        .map(predObjList -> (GraphPattern) GraphPatterns
            .tp(Rdf.iri(subject.stringValue()), predObjList))
        .reduce(pattern, (acc, triplePattern) -> acc.and(GraphPatterns.optional(triplePattern)));

    String selectQueryStr = selectQuery
        .where(pattern)
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
        .targetClass(findRequiredPropertyIri(nodeShape, SHACL.TARGET_CLASS))
        .propertyShapes(buildPropertyShapes(nodeShape))
        .build();
  }

  private Map<String, PropertyShape> buildPropertyShapes(Resource nodeShape) {
    return Models
        .getPropertyResources(shapeModel, nodeShape, SHACL.PROPERTY)
        .stream()
        .map(shape -> PropertyShape.builder()
            .name(findRequiredPropertyLiteral(shape, SHACL.NAME).stringValue())
            .path(findRequiredPropertyIri(shape, SHACL.PATH))
            .minCount(findPropertyLiteral(shape, SHACL.MIN_COUNT)
                .map(Literal::intValue)
                .orElse(0))
            .maxCount(findPropertyLiteral(shape, SHACL.MAX_COUNT)
                .map(Literal::intValue)
                .orElse(Integer.MAX_VALUE))
            .build())
        .collect(Collectors.toMap(PropertyShape::getName, Function.identity()));
  }

  private Optional<IRI> findPropertyIri(Resource shape, IRI predicate) {
    return Models.getPropertyIRI(shapeModel, shape, predicate);
  }

  private IRI findRequiredPropertyIri(Resource shape, IRI predicate) {
    return findPropertyIri(shape, predicate)
        .orElseThrow(() -> new InvalidConfigurationException(String
            .format("Shape '%s' requires a '%s' IRI property.", shape, predicate)));
  }

  private Optional<Literal> findPropertyLiteral(Resource shape, IRI predicate) {
    return Models.getPropertyLiteral(shapeModel, shape, predicate);
  }

  private Literal findRequiredPropertyLiteral(Resource shape, IRI predicate) {
    return findPropertyLiteral(shape, predicate)
        .orElseThrow(() -> new InvalidConfigurationException(String
            .format("Shape '%s' requires a '%s' literal property.", shape, predicate)));
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
