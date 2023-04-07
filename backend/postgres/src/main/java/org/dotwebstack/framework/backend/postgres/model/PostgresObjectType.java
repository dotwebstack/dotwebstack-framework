package org.dotwebstack.framework.backend.postgres.model;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.model.AbstractObjectField;
import org.dotwebstack.framework.core.model.AbstractObjectType;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectType extends AbstractObjectType<PostgresObjectField> {

  private String table;

  private boolean distinct;

  @Override
  public boolean isNested() {
    return StringUtils.isBlank(table);
  }

  public PostgresObjectType() {
    super();
  }

  public PostgresObjectType(PostgresObjectType objectType, List<PostgresObjectField> ancestors) {
    super();
    this.name = objectType.getName();
    this.filters = objectType.getFilters();
    this.sortableBy = objectType.getSortableBy();

    this.table = objectType.getTable();
    this.distinct = objectType.isDistinct();

    var fields = objectType.getFields()
        .values()
        .stream()
        .map(PostgresObjectField::new)
        .collect(Collectors.toList());

    fields.forEach(field -> field.setObjectType(this));
    fields.forEach(field -> field.initColumns(ancestors));

    this.fields = fields.stream()
        .collect(Collectors.toMap(AbstractObjectField::getName, field -> field));
  }
}
