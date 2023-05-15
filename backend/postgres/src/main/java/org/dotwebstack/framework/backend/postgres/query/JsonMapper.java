package org.dotwebstack.framework.backend.postgres.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.backend.query.AbstractObjectMapper;
import org.jooq.Field;
import org.jooq.JSON;

public class JsonMapper extends AbstractObjectMapper<Map<String, Object>> {

  private final Field<JSON> column;

  public JsonMapper(Field<JSON> column) {
    this.column = column;
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> row) {
    var rowVal = row.get(column.getName());

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
