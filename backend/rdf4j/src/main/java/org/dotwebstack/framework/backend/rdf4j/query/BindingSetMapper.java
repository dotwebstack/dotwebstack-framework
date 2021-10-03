package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.query.AbstractRowMapper;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import reactor.core.publisher.Flux;

@Getter
class BindingSetMapper extends AbstractRowMapper<BindingSet> {

  private String alias;

  public BindingSetMapper() {
  }

  public BindingSetMapper(String alias) {
    this.alias = alias;
  }

  @Override
  public Object apply(BindingSet bindings) {
    if (alias != null && !bindings.hasBinding(alias)) {
      return Optional.empty();
    }

    return super.apply(bindings);
  }

  public BindingSetMapper createNestedResultMapper(String name, String alias) {
    var resultMapper = new BindingSetMapper(alias);
    fieldMappers.put(name, resultMapper);
    return resultMapper;
  }

  @SuppressWarnings("unchecked")
  public Flux<Map<String, Object>> map(TupleQueryResult queryResult) {
    return Flux.fromIterable(queryResult)
        .map(row -> (Map<String, Object>) super.apply(row));
  }
}
