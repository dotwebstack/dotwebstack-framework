package org.dotwebstack.framework.core.backend.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.model.ObjectField;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
class AbstractObjectMapperTest {

  private RowMapper<Map<String, Object>> mapper;

  @BeforeEach
  void setUp() {
    mapper = new RowMapper<>();
  }

  @Test
  void apply_returnsMap() {
    mapper.register("ff", mock(FieldMapper.class));
    Map<String, Object> row = Map.of("aa", Map.of("b", "c"));

    var result = mapper.apply(row);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof Map);
    assertTrue(result.size() == 1);
  }

  @Test
  void register_addFieldMapper() {
    var mapperMock = mock(FieldMapper.class);

    mapper.register("any", mapperMock);

    assertThat(mapper.getFieldMapper("any"), is(mapperMock));
  }

  @Test
  void getLeafFieldMapper_throwsExpeption() {
    var mapperMock = mock(ObjectFieldMapper.class);
    var scalarFieldMapper = mock(ScalarFieldMapper.class);
    when(mapperMock.getFieldMapper(anyString())).thenReturn(scalarFieldMapper);
    mapper.register("aaa", mapperMock);

    List<ObjectField> fieldPath = new ArrayList<>();
    var fieldMock = mock(ObjectField.class);
    when(fieldMock.getName()).thenReturn("aaa");
    fieldPath.add(fieldMock);

    var exception = assertThrows(IllegalArgumentException.class, () -> mapper.getLeafFieldMapper(fieldPath));

    assertThat(exception.getMessage(), CoreMatchers.is("Scalar field mapper aaa not found."));
  }

  @Test
  void getLeafFieldMapper_returnsScalarFieldMapper() {
    var scalarFieldMapper = mock(ScalarFieldMapper.class);
    mapper.register("aaa", scalarFieldMapper);

    List<ObjectField> fieldPath = new ArrayList<>();
    var fieldMock = mock(ObjectField.class);
    when(fieldMock.getName()).thenReturn("aaa");
    fieldPath.add(fieldMock);

    var result = mapper.getLeafFieldMapper(fieldPath);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof ScalarFieldMapper);
  }

  @Test
  void getLeafFieldMapper_returnsScalarFieldMapper_ifNestedFieldMapper() {
    var objectFieldMapper = mock(ObjectFieldMapper.class);
    mapper.register("aaa", objectFieldMapper);
    var objectFieldMapper2 = mock(ScalarFieldMapper.class);
    when(objectFieldMapper.getFieldMapper(eq("bbb"))).thenReturn(objectFieldMapper2);
    mapper.register("bbb", objectFieldMapper2);

    List<ObjectField> fieldPath = new ArrayList<>();
    var fieldMock = mock(ObjectField.class);
    when(fieldMock.getName()).thenReturn("aaa");
    fieldPath.add(fieldMock);
    var fieldMock2 = mock(ObjectField.class);
    when(fieldMock2.getName()).thenReturn("bbb");
    fieldPath.add(fieldMock2);

    var result = mapper.getLeafFieldMapper(fieldPath);

    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof ScalarFieldMapper);
  }
}
