package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.jooq.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class ColumnMapperTest {

  @Test
  void initObject() {
    Field<Object> column = mock(Field.class);
    when(column.getName()).thenReturn("anyName");

    var result = new ColumnMapper(column);
    assertThat(result.getColumn(), CoreMatchers.is(column));
    assertThat(result.getAlias(), CoreMatchers.is(column.getName()));
  }

  @Test
  void apply_returnsObject_forMap() {
    Field<Object> column = mock(Field.class);
    when(column.getName()).thenReturn("anyName");
    Map<String, Object> map = Map.of("anyName", "bbb");

    var result = new ColumnMapper(column).apply(map);
    assertThat(result, CoreMatchers.is("bbb"));
  }
}
