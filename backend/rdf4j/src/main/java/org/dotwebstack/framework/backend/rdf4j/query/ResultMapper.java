package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.query.TupleQueryResult;
import reactor.core.publisher.Flux;

class ResultMapper {

  private final Map<String, FieldMapper> fieldMappers = new HashMap<>();

  public ResultMapper registerFieldMapper(String name, FieldMapper fieldMapper) {
    fieldMappers.put(name, fieldMapper);
    return this;
  }

  public Flux<Map<String, Object>> map(TupleQueryResult queryResult) {
    return Flux.fromIterable(queryResult)
        .map(bindings -> fieldMappers.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()
                .apply(bindings))));
  }
}
