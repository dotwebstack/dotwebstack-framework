package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.AbstractRowMapper;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import reactor.core.publisher.Flux;

class RowMapper extends AbstractRowMapper<BindingSet> {

  @SuppressWarnings("unchecked")
  public Flux<Map<String, Object>> map(TupleQueryResult queryResult) {
    return Flux.fromIterable(queryResult)
        .map(row -> (Map<String, Object>) super.apply(row));
  }
}
