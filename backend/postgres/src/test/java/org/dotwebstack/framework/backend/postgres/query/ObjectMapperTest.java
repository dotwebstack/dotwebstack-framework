package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.backend.query.FieldMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked"})
class ObjectMapperTest {

  private ObjectMapper mapper;

  @Test
  void apply_returnsMap_forGetAliasNotNull() {
    mapper = new ObjectMapper("aa");
    mapper.register("ff", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("aa", Map.of("b", "c"));

    var result = mapper.apply(row);

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
  }

  @Test
  void apply_returnsNull_forGetAliasNull() {
    mapper = new ObjectMapper("aa");
    mapper.register("ff", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("bbb", Map.of("b", "c"));

    var result = mapper.apply(row);

    assertThat(result, is(nullValue()));
  }

  @Test
  void apply_returnsMap_forIsAliasNull() {
    mapper = new ObjectMapper();
    mapper.register("ff", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("bbb", Map.of("b", "c"));

    var result = mapper.apply(row);

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
  }

  @Test
  void apply_returnsMap_forGetPresenceAliasTrue() {
    mapper = new ObjectMapper(null, "bar");
    mapper.register("foo", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("bar", true, "foo", Map.of("foo", "test"));

    var result = mapper.apply(row);

    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
  }

  @Test
  void apply_returnsNull_forGetPresenceAliasFalse() {
    mapper = new ObjectMapper(null, "bar");
    mapper.register("foo", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("bar", false);

    var result = mapper.apply(row);

    assertThat(result, is(nullValue()));
  }

  @Test
  void apply_returnsNull_forGetPresenceAliasNull() {
    mapper = new ObjectMapper(null, "bar");
    mapper.register("foo", mock(FieldMapper.class));
    Map<String, Object> row = new HashMap<>();
    row.put("bar", null);

    var result = mapper.apply(row);

    assertThat(result, is(nullValue()));
  }
}
