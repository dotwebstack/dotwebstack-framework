package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.dotwebstack.framework.core.backend.query.FieldMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})

public class ObjectMapperTest {

  private ObjectMapper mapper;

  @Test
  void apply_returnsMap_forGetAliasNotNull() {
    mapper = new ObjectMapper("aa");
    mapper.register("ff", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("aa", Map.of("b", "c"));

    var result = mapper.apply(row);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Map);
    assertTrue(result.size() == 1);
  }

  @Test
  void apply_returnsNull_forGetAliasNull() {
    mapper = new ObjectMapper("aa");
    mapper.register("ff", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("bbb", Map.of("b", "c"));

    var result = mapper.apply(row);
    assertNull(result);
  }

  @Test
  void apply_returnsMap_forIsAliasNull() {
    mapper = new ObjectMapper();
    mapper.register("ff", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("bbb", Map.of("b", "c"));

    var result = mapper.apply(row);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Map);
    assertTrue(result.size() == 1);
  }
}
