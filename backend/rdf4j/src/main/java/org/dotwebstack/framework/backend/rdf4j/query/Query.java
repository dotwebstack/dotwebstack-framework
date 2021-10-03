package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.applyCardinality;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.createTypePatterns;
import static org.dotwebstack.framework.backend.rdf4j.query.QueryHelper.getObjectField;

import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.rdf4j.model.Rdf4jObjectField;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.OrderBy;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
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

  private final RowMapper rowMapper = new RowMapper();

  private final SelectQuery selectQuery;

  public Query(ObjectRequest objectRequest, NodeShape nodeShape) {
    selectQuery = createSelect(objectRequest, nodeShape);
  }

  public Query(CollectionRequest collectionRequest, NodeShape nodeShape) {
    selectQuery = createSelect(collectionRequest, nodeShape);
  }

  public Flux<Map<String, Object>> execute(RepositoryConnection connection) {
    var queryString = selectQuery.getQueryString();

    LOG.debug("Executing query: {}", queryString);

    var queryResult = connection.prepareTupleQuery(queryString)
        .evaluate();

    return rowMapper.map(queryResult);
  }

  private SelectQuery createSelect(CollectionRequest collectionRequest, NodeShape nodeShape) {
    return createSelect(collectionRequest.getObjectRequest(), nodeShape).orderBy(createOrderBy(collectionRequest));
  }

  private SelectQuery createSelect(ObjectRequest objectRequest, NodeShape nodeShape) {
    var subject = SparqlBuilder.var(newAlias());
    var wherePattern = createWherePattern(objectRequest, nodeShape, subject, rowMapper);

    return Queries.SELECT()
        .where(wherePattern);
  }

  private GraphPattern createWherePattern(ObjectRequest objectRequest, NodeShape nodeShape, Variable subject,
      ObjectFieldMapper<BindingSet> fieldMapper) {
    var typePatterns = createTypePatterns(subject, SparqlBuilder.var(newAlias()), nodeShape);
    var patterns = new ArrayList<>(typePatterns);

    objectRequest.getSelectedScalarFields()
        .stream()
        .flatMap(field -> createWherePattern(field, getObjectField(objectRequest, field.getName()), nodeShape, subject,
            fieldMapper))
        .forEach(patterns::add);

    objectRequest.getSelectedObjectFields()
        .entrySet()
        .stream()
        .map(entry -> createNestedWherePattern(entry.getKey(), nodeShape.getPropertyShape(entry.getKey()
            .getName()), entry.getValue(), subject, fieldMapper))
        .forEach(patterns::add);

    return GraphPatterns.and(patterns.toArray(GraphPattern[]::new));
  }

  private Stream<GraphPattern> createWherePattern(SelectedField selectedField, Rdf4jObjectField objectField,
      NodeShape nodeShape, Variable subject, ObjectFieldMapper<BindingSet> fieldMapper) {
    if (objectField.isResource()) {
      fieldMapper.register(selectedField.getName(), new BindingMapper(subject.getQueryString()
          .substring(1)));
      return Stream.empty();
    }

    var objectAlias = newAlias();
    var propertyShape = nodeShape.getPropertyShape(selectedField.getName());

    fieldMapper.register(selectedField.getName(), new BindingMapper(objectAlias));

    return Stream
        .of(applyCardinality(propertyShape, subject.has(propertyShape.toPredicate(), SparqlBuilder.var(objectAlias))));
  }

  private GraphPattern createNestedWherePattern(SelectedField selectedField, PropertyShape propertyShape,
      ObjectRequest objectRequest, Variable subject, ObjectFieldMapper<BindingSet> fieldMapper) {
    var nestedResourceMapper = new BindingSetMapper(newAlias());
    var nestedResource = SparqlBuilder.var(nestedResourceMapper.getAlias());

    fieldMapper.register(selectedField.getName(), nestedResourceMapper);

    var nestedPattern = GraphPatterns.tp(subject, propertyShape.toPredicate(), nestedResource)
        .and(createWherePattern(objectRequest, propertyShape.getNode(), nestedResource, nestedResourceMapper));

    return applyCardinality(propertyShape, nestedPattern);
  }

  private OrderBy createOrderBy(CollectionRequest collectionRequest) {
    var sortCriterias = collectionRequest.getSortCriterias();

    if (sortCriterias.isEmpty()) {
      return null;
    }

    var orderables = sortCriterias.stream()
        .map(this::createOrderable)
        .collect(Collectors.toList());

    return SparqlBuilder.orderBy(orderables.toArray(Orderable[]::new));
  }

  private Orderable createOrderable(SortCriteria sortCriteria) {
    var fieldMapper = rowMapper.getLeafFieldMapper(sortCriteria.getFields());
    var orderable = SparqlBuilder.var(fieldMapper.getAlias());

    return SortDirection.ASC.equals(sortCriteria.getDirection()) ? orderable : orderable.desc();
  }

  private String newAlias() {
    return "x".concat(String.valueOf(aliasCounter.incrementAndGet()));
  }
}
