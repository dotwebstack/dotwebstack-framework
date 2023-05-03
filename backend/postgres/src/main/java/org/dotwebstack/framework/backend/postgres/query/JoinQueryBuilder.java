package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.helpers.ValidationHelper.validateFields;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.invertOnList;
import static org.dotwebstack.framework.backend.postgres.query.Query.EXISTS_KEY;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.columnName;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Accessors(fluent = true)
@Setter
class JoinQueryBuilder {

  @NotNull
  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  @NotNull
  private AliasManager aliasManager;

  @NotNull
  private SelectQuery<Record> dataQuery;

  @NotNull
  private Set<Map<String, Object>> joinKeys;

  @NotNull
  private JoinConfiguration joinConfiguration;

  private ContextCriteria contextCriteria;

  private Table<Record> table;

  @Setter(AccessLevel.NONE)
  private List<JoinColumn> joinColumns = new ArrayList<>();

  private String fullTableName;

  private JoinQueryBuilder() {}

  static JoinQueryBuilder newJoinQuery() {
    return new JoinQueryBuilder();
  }

  List<JoinColumn> build() {
    validateFields(this);

    if (joinConfiguration.getMappedBy() != null) {
      var mappedBy = joinConfiguration.getMappedBy();

      var mappedByJoinConfiguration = JoinConfiguration.builder()
          .objectField(mappedBy)
          .joinTable(JoinHelper.invert(mappedBy.getJoinTable()))
          .joinColumns(mappedBy.getJoinColumns())
          .objectType((PostgresObjectType) joinConfiguration.getObjectField()
              .getObjectType())
          .targetType((PostgresObjectType) mappedBy.getObjectType())
          .build();

      return newJoinQuery().joinConfiguration(mappedByJoinConfiguration)
          .contextCriteria(contextCriteria)
          .aliasManager(aliasManager)
          .fieldMapper(fieldMapper)
          .dataQuery(dataQuery)
          .table(table)
          .joinKeys(joinKeys)
          .build();
    }

    if (!joinConfiguration.getJoinColumns()
        .isEmpty()) {
      joinQuery(invertOnList(joinConfiguration.getObjectField(), joinConfiguration.getJoinColumns()), table);
    }

    if (joinConfiguration.getJoinTable() != null) {
      queryWithJoinTable();
    }

    if (!joinColumns.isEmpty()) {
      return joinColumns;
    }

    throw illegalArgumentException("Object field '{}' has no relation configuration!",
        joinConfiguration.getObjectField()
            .getName());
  }

  private void joinQuery(List<JoinColumn> joinColumns, Table<Record> joinConditionTable) {

//    if (joinConfiguration.getJoinTable() != null && joinConfiguration.getJoinTable().getInverseTableName().equals(fullTableName)) {

      var objectType = joinConfiguration.getObjectType();

      var keyJoinColumnAliasMap = joinColumns.stream()
          .collect(Collectors.toMap(joinColumn -> joinColumn, JoinColumn::getReferencedColumn));

      this.joinColumns.addAll(keyJoinColumnAliasMap.keySet());

      var keyColumnAliases = keyJoinColumnAliasMap.entrySet()
          .stream()
          .collect(Collectors.toMap(e -> columnName(e.getKey(), objectType), Map.Entry::getValue));

//          var keyTable = createValuesTable(keyColumnAliases, joinKeys);

          keyJoinColumnAliasMap.entrySet()
              .stream()
              .map(entry -> QueryHelper.column(joinConditionTable, entry.getKey()
                  .getName())
                  .equal(DSL.field(DSL.name(table.getName(), entry.getValue()))))
              .forEach(dataQuery::addConditions);
//    }
    addExistsJoinColumns(dataQuery, joinColumns, joinConditionTable);
  }

  private void queryWithJoinTable() {
    var joinTable = joinConfiguration.getJoinTable();

    var junctionTable = QueryHelper.findTable(joinTable.getName(), contextCriteria)
        .as(aliasManager.newAlias());

    dataQuery.addFrom(junctionTable);

    var joinConditions = createJoinConditions(junctionTable, table, joinTable.getInverseJoinColumns(),
        joinConfiguration.getTargetType());

    dataQuery.addConditions(joinConditions);
  }

  private void addExistsJoinColumns(SelectQuery<Record> dataQuery, List<JoinColumn> joinColumns, Table<Record> table) {
    var columnsNames = joinColumns.stream()
        .map(JoinColumn::getName)
        .collect(Collectors.toList());

    addExists(dataQuery, columnsNames, table);
  }

  private void addExists(SelectQuery<Record> dataQuery, Collection<String> columnNames, Table<Record> table) {

    var columnAliases = columnNames.stream()
        .collect(Collectors.toMap(Function.identity(), columnName -> aliasManager.newAlias()));

  //TODO: misschien gevolgen voor andere dingen?
    columnAliases.entrySet()
        .stream()
        .map(entry -> QueryHelper.column(table, entry.getKey())
            .as(entry.getValue()))
        .forEach(dataQuery::addSelect);
    register(EXISTS_KEY, columnAliases);
  }

  private void register(String name, Map<String, String> columnAliases) {
    fieldMapper.register(name, row -> columnAliases.entrySet()
        .stream()
        .filter(entry -> !Objects.isNull(row.get(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> row.get(entry.getValue()), (prev, next) -> next,
            HashMap::new)));
  }
}
