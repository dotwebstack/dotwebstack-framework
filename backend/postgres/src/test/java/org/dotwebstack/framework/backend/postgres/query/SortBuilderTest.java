package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.SortBuilder.newSorting;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.backend.query.ScalarFieldMapper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class SortBuilderTest {

  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private List<SortCriteria> sortCriteriaList;

  private SortBuilder sortBuilder;

  @BeforeEach
  void setUp() {
    fieldMapper = mock(ObjectFieldMapper.class);
    sortBuilder = newSorting();
  }

  @Test
  void build_returnsList_forAscSortCriteria() {
    initSortCriteriaList(SortDirection.ASC);

    var result = sortBuilder.sortCriterias(sortCriteriaList)
        .fieldMapper(fieldMapper)
        .build();

    assertThat(result.get(0)
        .toString(), CoreMatchers.is("\"a\" asc"));
  }

  @Test
  void build_returnsList_forDescSortCriteria() {
    initSortCriteriaList(SortDirection.DESC);

    var result = sortBuilder.sortCriterias(sortCriteriaList)
        .fieldMapper(fieldMapper)
        .build();

    assertThat(result.get(0)
        .toString(), CoreMatchers.is("\"a\" desc"));
  }

  private void initSortCriteriaList(SortDirection direction) {
    ObjectFieldMapper objectFieldMapper = mock(ObjectFieldMapper.class);
    ObjectFieldMapper objectFieldMapper2 = mock(ObjectFieldMapper.class);
    lenient().when(objectFieldMapper2.getFieldMapper(any(String.class)))
        .thenReturn(objectFieldMapper);
    lenient().when(objectFieldMapper2.getFieldMapper(any(String.class)))
        .thenReturn(objectFieldMapper);
    lenient().when(fieldMapper.getFieldMapper("a"))
        .thenReturn(objectFieldMapper2);

    PostgresObjectField sortCriteriaField = mock(PostgresObjectField.class);
    lenient().when(sortCriteriaField.getColumn())
        .thenReturn("a");
    lenient().when(sortCriteriaField.getName())
        .thenReturn("a");
    var fieldPath = List.of((ObjectField) sortCriteriaField);

    SortCriteria.SortCriteriaBuilder sortCriteria = SortCriteria.builder()
        .fieldPath(fieldPath)
        .direction(direction);
    sortCriteriaList = List.of(sortCriteria.build());

    ScalarFieldMapper<Map<String, Object>> leafFieldMapper = mock(ScalarFieldMapper.class);
    lenient().when(leafFieldMapper.getAlias())
        .thenReturn("a");

    lenient().when(fieldMapper.getLeafFieldMapper(fieldPath))
        .thenReturn(leafFieldMapper);
  }
}
