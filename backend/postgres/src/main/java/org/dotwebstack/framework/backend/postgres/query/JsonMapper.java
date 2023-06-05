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

  private final ObjectMapper objectMapper;

  public JsonMapper(String columnName) {
    this.columnName = columnName;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> row) {
    var rowVal = row.get(columnName);

    if (rowVal == null) {
      return Map.of();
    }

    var jsonString = ((Json) rowVal).asString();
    try {
      var typeRef = new TypeReference<HashMap<String, Object>>() {};
      return objectMapper.readValue(jsonString, typeRef);
    } catch (JsonProcessingException e) {
      throw new JsonMappingException("Unable to convert Json column to GraphQL type.", e);
    }
  }
}
