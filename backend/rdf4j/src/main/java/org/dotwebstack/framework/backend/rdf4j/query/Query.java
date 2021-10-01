package org.dotwebstack.framework.backend.rdf4j.query;

import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class Query {

  private static final Logger LOG = LoggerFactory.getLogger(Query.class);

  private final AtomicInteger aliasCounter = new AtomicInteger();

  private final ResultMapper finalResultMapper = new ResultMapper();

  private final SelectQuery selectQuery;

  public Query(ObjectRequest objectRequest, NodeShape nodeShape) {
    selectQuery = createSelect(objectRequest, nodeShape);
  }

  public Query(CollectionRequest collectionRequest, NodeShape nodeShape) {
    selectQuery = createSelect(collectionRequest, nodeShape);
  }

  public Flux<Map<String, Object>> execute(Repository repository) {
    var connection = repository.getConnection();
    var queryString = selectQuery.getQueryString();

    LOG.debug("Executing query: {}", queryString);

    var queryResult = connection.prepareTupleQuery(queryString)
        .evaluate();

    return finalResultMapper.map(queryResult)
        .doOnComplete(connection::close);
  }

  private SelectQuery createSelect(CollectionRequest collectionRequest, NodeShape nodeShape) {
    return createSelect(collectionRequest.getObjectRequest(), nodeShape);
  }

  private SelectQuery createSelect(ObjectRequest objectRequest, NodeShape nodeShape) {
    var subject = SparqlBuilder.var(newAlias());
    var wherePattern = createWherePattern(objectRequest, nodeShape, subject, finalResultMapper);

    return Queries.SELECT()
        .where(wherePattern);
  }

  private GraphPattern createWherePattern(ObjectRequest objectRequest, NodeShape nodeShape, Variable subject,
      ResultMapper resultMapper) {
    var typePatterns = QueryHelper.createTypePatterns(subject, SparqlBuilder.var(newAlias()), nodeShape);
    var patterns = new ArrayList<>(typePatterns);

    objectRequest.getSelectedScalarFields()
        .stream()
        .map(field -> createWherePattern(field, nodeShape.getPropertyShape(field.getName()), subject, resultMapper))
        .forEach(patterns::add);

    objectRequest.getSelectedObjectFields()
        .entrySet()
        .stream()
        .map(entry -> createNestedWherePattern(entry.getKey(), nodeShape.getPropertyShape(entry.getKey()
            .getName()), entry.getValue(), subject, resultMapper))
        .forEach(patterns::add);

    return GraphPatterns.and(patterns.toArray(GraphPattern[]::new));
  }

  private GraphPattern createWherePattern(SelectedField selectedField, PropertyShape propertyShape,
      Variable subject, ResultMapper resultMapper) {
    var objectAlias = newAlias();
    resultMapper.registerFieldMapper(selectedField.getName(), QueryHelper.createFieldMapper(objectAlias));

    return QueryHelper.applyCardinality(propertyShape,
        subject.has(propertyShape.toPredicate(), SparqlBuilder.var(objectAlias)));
  }

  private GraphPattern createNestedWherePattern(SelectedField selectedField, PropertyShape propertyShape,
      ObjectRequest objectRequest, Variable subject, ResultMapper resultMapper) {
    var nestedResource = SparqlBuilder.var(newAlias());
    var nestedResultMapper = resultMapper.nestedResultMapper(selectedField.getName());

    var nestedPattern = GraphPatterns.tp(subject, propertyShape.toPredicate(), nestedResource)
        .and(createWherePattern(objectRequest, propertyShape.getNode(), nestedResource, nestedResultMapper));

    return QueryHelper.applyCardinality(propertyShape, nestedPattern);
  }

  private String newAlias() {
    return "x".concat(String.valueOf(aliasCounter.incrementAndGet()));
  }
}
