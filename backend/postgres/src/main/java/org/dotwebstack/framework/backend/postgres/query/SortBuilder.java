package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.jooq.SortField;

@Accessors(fluent = true)
@Setter
class SortBuilder {

  @NotNull
  private List<SortCriteria> sortCriterias;

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private SortBuilder() {}

  static SortBuilder newSorting() {
    return new SortBuilder();
  }

  List<SortField<Object>> build() {
    validateFields(this);

    return sortCriterias.stream()
        .map(this::createSortCondition)
        .collect(Collectors.toList());
  }

  private SortField<Object> createSortCondition(SortCriteria sortCriteria) {
    List<ObjectField> fieldPath = sortCriteria.getFieldPath();
    var leafFieldMapper = fieldMapper.getLeafFieldMapper(fieldPath);

    var sortField = column(null, leafFieldMapper.getAlias());

    switch (sortCriteria.getDirection()) {
      case ASC:
        return sortField.asc();
      case DESC:
        return sortField.desc();
      default:
        throw unsupportedOperationException("Unsupported direction: {}", sortCriteria.getDirection());
    }
  }
}
