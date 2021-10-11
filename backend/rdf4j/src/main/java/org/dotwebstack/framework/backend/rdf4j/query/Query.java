package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.RowMapper;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.OrderBy;
import org.eclipse.rdf4j.sparqlbuilder.core.Orderable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public class Query {

  private static final Logger LOG = LoggerFactory.getLogger(Query.class);

  private final AliasManager aliasManager = new AliasManager();

  private final RowMapper<BindingSet> rowMapper = new RowMapper<>();

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

    return Flux.fromIterable(queryResult)
        .map(rowMapper);
  }

  private SelectQuery createSelect(CollectionRequest collectionRequest, NodeShape nodeShape) {
    var query = createSelect(collectionRequest.getObjectRequest(), nodeShape);
    var sortCriterias = collectionRequest.getSortCriterias();

    if (!sortCriterias.isEmpty()) {
      query = query.orderBy(createOrderBy(sortCriterias));
    }

    return query;
  }

  private SelectQuery createSelect(ObjectRequest objectRequest, NodeShape nodeShape) {
    return Queries.SELECT()
        .where(createPattern(objectRequest, nodeShape));
  }

  private GraphPattern createPattern(ObjectRequest objectRequest, NodeShape nodeShape) {
    var subject = SparqlBuilder.var(aliasManager.newAlias());

    return GraphPatternBuilder.newGraphPattern()
        .objectRequest(objectRequest)
        .nodeShape(nodeShape)
        .subject(subject)
        .fieldMapper(rowMapper)
        .aliasManager(aliasManager)
        .build();
  }

  private OrderBy createOrderBy(List<SortCriteria> sortCriterias) {
    if (sortCriterias.isEmpty()) {
      throw illegalArgumentException("Sort criteria is empty.");
    }

    var orderables = sortCriterias.stream()
        .map(this::createOrderable)
        .collect(Collectors.toList());

    return SparqlBuilder.orderBy(orderables.toArray(Orderable[]::new));
  }

  private Orderable createOrderable(SortCriteria sortCriteria) {
    var leafFieldMapper = rowMapper.getLeafFieldMapper(sortCriteria.getFields());
    var orderable = SparqlBuilder.var(leafFieldMapper.getAlias());

    return SortDirection.ASC.equals(sortCriteria.getDirection()) ? orderable : orderable.desc();
  }
}
