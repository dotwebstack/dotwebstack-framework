package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.OrderBy;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class Query {

  private static final Logger LOG = LoggerFactory.getLogger(Query.class);

  private final AliasManager aliasManager = new AliasManager();

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
    var subject = SparqlBuilder.var(aliasManager.newAlias());

    var wherePatternFactory = WherePatternFactory.builder()
        .objectRequest(objectRequest)
        .nodeShape(nodeShape)
        .subject(subject)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .build();

    return Queries.SELECT()
        .where(wherePatternFactory.create());
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
}
