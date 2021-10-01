package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import reactor.core.publisher.Flux;

class ResultMapper implements FieldMapper<Map<String, Object>> {

  private final Map<String, FieldMapper<?>> fieldMappers = new HashMap<>();

  @Override
  public Map<String, Object> apply(BindingSet bindings) {
    return fieldMappers.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()
            .apply(bindings)));
  }

  public void registerFieldMapper(String name, FieldMapper<?> fieldMapper) {
    fieldMappers.put(name, fieldMapper);
  }

  public ResultMapper nestedResultMapper(String name) {
    var resultMapper = new ResultMapper();
    fieldMappers.put(name, resultMapper);
    return resultMapper;
  }

  public Flux<Map<String, Object>> map(TupleQueryResult queryResult) {
    return Flux.fromIterable(queryResult)
        .map(this);
  }
}
