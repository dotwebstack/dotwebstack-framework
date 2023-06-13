package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.r2dbc.postgresql.codec.Json;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.DotWebStackRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonMapperTest {

  @Test
  void apply_returnsMap_forJson() {
    var jsonMapper = new JsonMapper("json");

    var json = Json.of("""
        {
            "name": "Jaime Perkins",
            "email": "jaimeperkins@kiosk.com",
            "friends": [
              {
                "name": "Riddle Cobb"
              }
            ]
          }""");

    Map<String, Object> row = Map.of("json", json);

    var result = jsonMapper.apply(row);

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(3));
    assertThat(result.get("friends"), instanceOf(ArrayList.class));
  }

  @Test
  void apply_returnEmptyMap_forNull() {
    var jsonMapper = new JsonMapper("json");
    var row = new HashMap<String, Object>();
    row.put("json", null);
    var result = jsonMapper.apply(row);

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(0));
  }

  @Test
  void apply_throwsException_forInvalidJson() {
    var jsonMapper = new JsonMapper("json");

    var json = Json.of("""
        {
            "name": "Jaime Perkins"
            "email": "jaimeperkins@kiosk.com"
          }""");

    Map<String, Object> row = Map.of("json", json);

    var exception = assertThrows(DotWebStackRuntimeException.class, () -> {
      jsonMapper.apply(row);
    });

    assertThat(exception.getMessage(), is("Unable to convert Json column to GraphQL type."));
  }
}
