package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.createJoinConditions;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;

@Accessors(fluent = true)
@Setter
class JoinBuilder {
  @NotNull
  private JoinConfiguration joinConfiguration;

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
    if (joinConfiguration.getMappedBy() != null) {
      var mappedBy = joinConfiguration.getMappedBy();
      return newJoin().table(relatedTable)
          .relatedTable(table)
          .joinConfiguration(JoinConfiguration.builder()
              .joinColumns(mappedBy.getJoinColumns())
              .joinTable(mappedBy.getJoinTable())
              .targetType((PostgresObjectType) mappedBy.getTargetType())
              .build())
          .tableCreator(tableCreator)
          .build();
    }

    if (!joinConfiguration.getJoinColumns()
        .isEmpty()) {
      // Normal join column
      return createJoinConditions(table, relatedTable, joinConfiguration.getJoinColumns(),
          joinConfiguration.getTargetType());
    }

    if (joinConfiguration.getJoinTable() != null) {
      var joinTable = joinConfiguration.getJoinTable();

      var junctionTable = tableCreator.apply(joinTable.getName());

      var leftSide =
          createJoinConditions(junctionTable, table, joinTable.getJoinColumns(), joinConfiguration.getObjectType());

      List<Condition> rightSide = new ArrayList<>();
      if (relatedTable != null) {
        rightSide.addAll(createJoinConditions(junctionTable, relatedTable, joinTable.getInverseJoinColumns(),
            joinConfiguration.getTargetType()));
      }

      return Stream.concat(leftSide.stream(), rightSide.stream())
          .collect(Collectors.toList());
    }

    throw illegalArgumentException("Object field '{}' has no relation configuration!",
        joinConfiguration.getObjectField()
            .getName());
  }
}
