package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.AbstractRowMapper;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import reactor.core.publisher.Flux;

@Getter
class BindingSetMapper extends AbstractRowMapper<BindingSet> {

  public BindingSetMapper createNestedResultMapper(String name) {
    var resultMapper = new BindingSetMapper();
    fieldMappers.put(name, resultMapper);
    return resultMapper;
  }

  public Flux<Map<String, Object>> map(TupleQueryResult queryResult) {
    return Flux.fromIterable(queryResult)
        .map(this);
  }
}
