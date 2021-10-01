package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import reactor.core.publisher.Flux;

@Getter
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

  public FieldMapper<?> getFieldMapper(String name) {
    return fieldMappers.get(name);
  }

  public ScalarFieldMapper getScalarFieldMapper(List<ObjectField> fieldPath) {
    var fields = new ArrayList<>(fieldPath);
    var finalField = fields.remove(fields.size() - 1);

    ResultMapper nestedMapper = this;
    for (var field : fields) {
      var nestedFieldMapper = nestedMapper.getFieldMapper(field.getName());

      if (nestedFieldMapper instanceof ResultMapper) {
        nestedMapper = (ResultMapper) nestedFieldMapper;
      } else {
        throw ExceptionHelper.illegalStateException("Field path is not correct.");
      }
    }

    var fieldMapper = nestedMapper.getFieldMapper(finalField.getName());

    if (fieldMapper instanceof ScalarFieldMapper) {
      return (ScalarFieldMapper) fieldMapper;
    }

    throw illegalArgumentException("Scalar field mapper {} not found.", finalField.getName());
  }

  public ResultMapper createNestedResultMapper(String name) {
    var resultMapper = new ResultMapper();
    fieldMappers.put(name, resultMapper);
    return resultMapper;
  }

  public Flux<Map<String, Object>> map(TupleQueryResult queryResult) {
    return Flux.fromIterable(queryResult)
        .map(this);
  }
}
