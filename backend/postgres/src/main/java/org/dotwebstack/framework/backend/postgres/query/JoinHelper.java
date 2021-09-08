package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.field;
import static org.dotwebstack.framework.backend.postgres.query.TableHelper.findTable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.Condition;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
public class JoinHelper {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  public JoinHelper(DotWebStackConfiguration dotWebStackConfiguration) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
  }

  public List<Condition> createJoinConditions(PostgresFieldConfiguration leftSideConfiguration, Table<?> leftSideTable,
      Table<?> rightSideTable, Map<String, String> fieldAliasMap) {

    if (leftSideConfiguration.getJoinColumns() != null) {
      var condition = getJoinCondition(leftSideConfiguration.getJoinColumns(),
          ((PostgresTypeConfiguration) leftSideConfiguration.getTypeConfiguration()).getFields(), rightSideTable,
          leftSideTable, fieldAliasMap);
      return List.of(condition);
    }

    if (leftSideConfiguration.getMappedBy() != null) {
      PostgresTypeConfiguration otherSideTypeConfiguration =
          (PostgresTypeConfiguration) dotWebStackConfiguration.getObjectTypes()
              .get(leftSideConfiguration.getType());
      PostgresFieldConfiguration otherSideFieldConfiguration =
          otherSideTypeConfiguration.getField(leftSideConfiguration.getMappedBy())
              .orElseThrow();

      return createJoinConditions(otherSideFieldConfiguration, rightSideTable, leftSideTable, Map.of());
    }

    return List.of();
  }

  public Condition getJoinTableCondition(PostgresTableField leftSide, PostgresTableType rightSide,
      Map<String, String> rightFieldAliasMap, Table<?> joinTable) {

    var joinColumns = leftSide.getFieldConfiguration()
        .findJoinColumns();

    var rightFields = rightSide.getTypeConfiguration()
        .getFields();

    return getJoinCondition(joinColumns, rightFields, joinTable, rightSide.getTable(), rightFieldAliasMap)
        .and(getInverseJoinCondition(leftSide, joinTable));
  }

  public Condition getJoinCondition(List<JoinColumn> joinColumns, Map<String, PostgresFieldConfiguration> fields,
      Table<?> leftSideTable, Table<?> rightSideTable, Map<String, String> rightFieldAliasMap) {

    return joinColumns.stream()
        .map(joinColumn -> {
          var otherSideFieldConfiguration = fields.get(joinColumn.getField());

          var leftColumn = field(leftSideTable, joinColumn.getName());

          var rightColumnAlias = rightFieldAliasMap.get(otherSideFieldConfiguration.getColumn());

          var rightColumn = field(rightSideTable,
              rightColumnAlias != null ? rightColumnAlias : otherSideFieldConfiguration.getColumn());

          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }

  public void addJoinTableCondition(SelectQuery<?> subSelect, ObjectSelectContext objectSelectContext,
      PostgresTableField leftSide, PostgresTableType rightSide, Map<String, String> fieldAliasMap,
      List<ContextCriteria> contextCriterias) {

    if (leftSide.getFieldConfiguration()
        .getJoinTable() != null) {
      var joinTable = findTable(leftSide.getFieldConfiguration()
          .getJoinTable()
          .getName(), contextCriterias).asTable(objectSelectContext.newTableAlias());
      var condition = getJoinTableCondition(leftSide, rightSide, fieldAliasMap, joinTable);

      // create join for query with single value jointable mapping & subscriptions
      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    }
  }

  public void addAggregateJoin(SelectQuery<?> subSelect, ObjectSelectContext objectSelectContext,
      PostgresTableField leftSide, PostgresTableType rightSide, List<ContextCriteria> contextCriterias) {

    var leftSideConfiguration = leftSide.getFieldConfiguration();
    var rightSideConfiguration = rightSide.getTypeConfiguration();

    if (leftSideConfiguration.getJoinTable() != null) {
      var joinTable = findTable(leftSideConfiguration.getJoinTable()
          .getName(), contextCriterias).asTable(objectSelectContext.newTableAlias());

      // create join with jointable and join condition on joinColumns and inverse joinColumn
      var condition = getJoinTableCondition(leftSide, rightSide, objectSelectContext.getFieldAliasMap(), joinTable);

      subSelect.addJoin(joinTable, JoinType.JOIN, condition);
    } else {
      var condition = getJoinCondition(leftSideConfiguration.getJoinColumns(), rightSideConfiguration.getFields(),
          leftSide.getTable(), rightSide.getTable(), objectSelectContext.getFieldAliasMap());
      subSelect.addConditions(condition);
    }
  }

  public Optional<Table<?>> createJoinTableForKeyCriteria(PostgresTypeConfiguration typeConfiguration,
      SelectQuery<?> query, ObjectSelectContext objectSelectContext, Table<?> table, ObjectRequest objectRequest) {

    return objectRequest.getKeyCriteria()
        .stream()
        .filter(PostgresKeyCriteria.class::isInstance)
        .map(PostgresKeyCriteria.class::cast)
        .findFirst()
        .filter(keyCriteria -> keyCriteria.getJoinTable() != null)
        .map(PostgresKeyCriteria::getJoinTable)
        .map(joinTable -> {
          var aliasedJoinTable = findTable(joinTable.getName(), objectRequest.getContextCriterias())
              .asTable(objectSelectContext.newTableAlias());

          var joinCondition = createJoinConditionForKeyCriteria(joinTable.getInverseJoinColumns(),
              typeConfiguration.getFields(), aliasedJoinTable, table);

          query.addJoin(aliasedJoinTable, JoinType.JOIN, joinCondition);

          return aliasedJoinTable;
        });
  }

  public Condition createJoinConditionForKeyCriteria(List<JoinColumn> joinColumns,
      Map<String, PostgresFieldConfiguration> fields, Table<?> leftSideTable, Table<?> rightSideTable) {
    return joinColumns.stream()
        .map(joinColumn -> {
          var otherSideFieldConfiguration = fields.get(joinColumn.getField());

          var leftColumn = field(leftSideTable, joinColumn.getName());
          var rightColumn = field(rightSideTable, otherSideFieldConfiguration.getColumn());

          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }

  private Condition getInverseJoinCondition(PostgresTableField leftPostgresTableField, Table<?> rightSideTable) {
    return leftPostgresTableField.getFieldConfiguration()
        .findInverseJoinColumns()
        .stream()
        .map(joinColumn -> {
          var otherSideFieldConfiguration = (PostgresFieldConfiguration) leftPostgresTableField.getFieldConfiguration()
              .getTypeConfiguration()
              .getFields()
              .get(joinColumn.getField());

          var leftColumn = field(rightSideTable, joinColumn.getName());
          var rightColumn = field(leftPostgresTableField.getTable(), otherSideFieldConfiguration.getColumn());

          return Objects.requireNonNull(leftColumn)
              .eq(rightColumn);
        })
        .reduce(DSL.noCondition(), Condition::and);
  }
}
