package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.eclipse.rdf4j.repository.RepositoryConnection;
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
    var patternFactory = createPatternFactory(collectionRequest.getObjectRequest(), nodeShape);
    var query = createSelect(patternFactory);

    if (collectionRequest.hasSortCriterias()) {
      query = query.orderBy(patternFactory.createOrderBy(collectionRequest.getSortCriterias()));
    }

    return query;
  }

  private SelectQuery createSelect(ObjectRequest objectRequest, NodeShape nodeShape) {
    var patternFactory = createPatternFactory(objectRequest, nodeShape);
    return createSelect(patternFactory);
  }

  private SelectQuery createSelect(GraphPatternFactory patternFactory) {
    return Queries.SELECT()
        .where(patternFactory.create());
  }

  private GraphPatternFactory createPatternFactory(ObjectRequest objectRequest, NodeShape nodeShape) {
    var subject = SparqlBuilder.var(aliasManager.newAlias());

    return GraphPatternFactory.builder()
        .objectRequest(objectRequest)
        .nodeShape(nodeShape)
        .subject(subject)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .build();
  }
}
