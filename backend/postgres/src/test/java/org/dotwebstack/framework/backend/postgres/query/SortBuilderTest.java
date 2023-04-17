package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.backend.query.RowMapper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SortBuilderTest {

  @ParameterizedTest
  @CsvSource({"ASC", "DESC"})
  void build_returnsList_forSortCriteria(String sortDirection) {
    List<SortCriteria> sortCriterias = createSortCriterias(sortDirection);

    ObjectFieldMapper<Map<String, Object>> rowMapper = createRowMapper();

    var result = SortBuilder.newSorting()
        .sortCriterias(sortCriterias)
        .fieldMapper(rowMapper)
        .build();

    assertThat(result.get(0)
        .toString(), is(String.format("\"x2\" %s", sortDirection.toLowerCase())));
    assertThat(result.get(1)
        .toString(), is(String.format("\"x3\" %s", sortDirection.toLowerCase())));
  }

  private List<SortCriteria> createSortCriterias(String sortDirection) {
    SortCriteria sortCriteria = SortCriteria.builder()
        .fieldPath(createFieldPath())
        .direction(SortDirection.valueOf(sortDirection))
        .build();

    SortCriteria nestedSortCriteria = SortCriteria.builder()
        .fieldPath(createNestedFieldPath())
        .direction(SortDirection.valueOf(sortDirection))
        .build();

    return List.of(sortCriteria, nestedSortCriteria);
  }

  private List<ObjectField> createFieldPath() {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName("fieldOne");
    return List.of(objectField);
  }

  private List<ObjectField> createNestedFieldPath() {
    PostgresObjectField objectField = new PostgresObjectField();
    objectField.setName("fieldTwo");

    PostgresObjectField nestedObjectField = new PostgresObjectField();
    nestedObjectField.setName("nestedField");

    return List.of(objectField, nestedObjectField);
  }

  private ObjectFieldMapper<Map<String, Object>> createRowMapper() {
    ObjectFieldMapper<Map<String, Object>> rowMapper = new RowMapper<>();

    rowMapper.register("fieldOne", new ColumnMapper(DSL.field("x2")
        .as("x2")));

    ObjectFieldMapper<Map<String, Object>> objectMapper = new ObjectMapper("x1");
    objectMapper.register("nestedField", new ColumnMapper(DSL.field("x3")
        .as("x3")));
    rowMapper.register("fieldTwo", objectMapper);

    return rowMapper;
  }

  @Test
  void build_throwsException_forMissingFields() {
    SortBuilder builder = SortBuilder.newSorting();
    var exception = assertThrows(ConstraintViolationException.class, builder::build);

    assertThat(exception.getMessage(),
        startsWith("class org.dotwebstack.framework.backend.postgres.query.SortBuilder has validation errors (2):"));
  }

}
