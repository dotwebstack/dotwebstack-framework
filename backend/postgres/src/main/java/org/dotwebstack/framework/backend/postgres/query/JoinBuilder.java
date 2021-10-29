package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  private PostgresObjectField current;

  private Table<Record> table;

  private Table<Record> relatedTable;

  private Function<String, Table<Record>> tableCreator;

  private JoinBuilder() {}

  static JoinBuilder newJoin() {
    return new JoinBuilder();
  }

  List<Condition> build() {
    validateFields(this);

    // Inverted mapped by
    if (current.getMappedByObjectField() != null) {
      return newJoin().table(relatedTable)
          .relatedTable(table)
          .current(current.getMappedByObjectField())
          .tableCreator(tableCreator)
          .build();
    }

    if (!current.getJoinColumns()
        .isEmpty()) {
      // Normal join column
      return createJoinConditions(table, relatedTable, current.getJoinColumns(),
          (PostgresObjectType) current.getTargetType());
    }

    if (current.getJoinTable() != null) {
      var joinTable = current.getJoinTable();

      var junctionTable = tableCreator.apply(joinTable.getName());

      var leftSide = createJoinConditions(junctionTable, table, joinTable.getJoinColumns(),
          (PostgresObjectType) current.getObjectType());

      var targetType =
          current.getAggregationOfType() != null ? current.getAggregationOfType() : current.getTargetType();

      var rightSide = createJoinConditions(junctionTable, relatedTable, joinTable.getInverseJoinColumns(),
          (PostgresObjectType) targetType);

      return Stream.concat(leftSide.stream(), rightSide.stream())
          .collect(Collectors.toList());
    }

    throw illegalArgumentException("Object field '{}' has no relation configuration!", current.getName());
  }
}
