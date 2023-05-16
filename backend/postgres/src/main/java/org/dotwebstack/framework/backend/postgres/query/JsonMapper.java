package org.dotwebstack.framework.backend.postgres.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.backend.query.AbstractObjectMapper;

public class JsonMapper extends AbstractObjectMapper<Map<String, Object>> {

  private final String columnName;

  public JsonMapper(String columnName) {
    this.columnName = columnName;
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> row) {
    var rowVal = row.get(columnName);

    if (rowVal == null) {
      return Map.of();
    }

    var objMapper = new ObjectMapper();
    var jsonString = ((Json) rowVal).asString();
    try {
      TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
      return objMapper.readValue(jsonString, typeRef);
    } catch (JsonProcessingException e) {
      throw new JsonMappingException("Unable to convert Json column to GraphQL type.", e);
    }
  }
}
