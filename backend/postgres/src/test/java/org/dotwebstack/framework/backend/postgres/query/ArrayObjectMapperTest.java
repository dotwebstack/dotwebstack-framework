package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.jooq.Field;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked"})
class ArrayObjectMapperTest {

  @Test
  void apply_returnsObject_forRow() {
    Map<String, Object> map = new HashMap<>();
    map.put("x1", new String[] {"foo_id1", "bar_id1"});
    map.put("x2", new String[] {"foo_id2", "bar_id2"});

    ArrayObjectMapper arrayObjectMapper = new ArrayObjectMapper();

    Field<Object> id1Column = mock(Field.class);
    when(id1Column.getName()).thenReturn("x1");
    arrayObjectMapper.register("id1", new ColumnMapper(id1Column));

    Field<Object> id2Column = mock(Field.class);
    when(id2Column.getName()).thenReturn("x2");
    arrayObjectMapper.register("id2", new ColumnMapper(id2Column));

    assertThat(arrayObjectMapper.apply(map), CoreMatchers
        .equalTo(List.of(Map.of("id2", "foo_id2", "id1", "foo_id1"), Map.of("id2", "bar_id2", "id1", "bar_id1"))));
  }
}
