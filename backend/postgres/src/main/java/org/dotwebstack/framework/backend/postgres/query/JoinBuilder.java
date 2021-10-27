package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;

@Accessors(fluent = true)
@Setter
class JoinBuilder {

  @NotNull
  private Table<Record> table;

  @NotNull
  private PostgresObjectField current;

  private Table<Record> relatedTable;

  private JoinBuilder() {}

  static JoinBuilder newJoin() {
    return new JoinBuilder();
  }

  List<Condition> build() {
    validateFields(this);

    // Inverted mapped by
    if (current.getMappedByObjectField() != null) {
      var mappedByObjectField = current.getMappedByObjectField();
      return mappedByObjectField.getJoinColumns()
          .stream()
          .map(joinColumn -> {
            var field = column(relatedTable, joinColumn.getName());
            var referencedField = column(table, joinColumn, (PostgresObjectType) current.getObjectType());
            return referencedField.equal(field);
          })
          .collect(Collectors.toList());
    }

    // Normal join column
    return current.getJoinColumns()
        .stream()
        .map(joinColumn -> {
          var field = column(table, joinColumn.getName());
          var referencedField = column(relatedTable, joinColumn, (PostgresObjectType) current.getTargetType());
          return referencedField.equal(field);
        })
        .collect(Collectors.toList());
  }
}
