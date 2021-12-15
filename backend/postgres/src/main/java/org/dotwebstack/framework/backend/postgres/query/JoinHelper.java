package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.column;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.ObjectType;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;


public class JoinHelper {

  private JoinHelper() {}

  public static List<JoinColumn> invertOnList(final PostgresObjectField objectField, List<JoinColumn> joinColumns) {
    return joinColumns.stream()
        .map(joinColumn -> resolveReferencedField(joinColumn, (PostgresObjectType) objectField.getTargetType()))
        .map(joinColumn -> {
          if (objectField.isList()) {
            return invert(joinColumn);
          }
          return joinColumn;
        })
        .collect(Collectors.toList());
  }

  private static JoinColumn invert(JoinColumn joinColumn) {
    var result = new JoinColumn();
    result.setName(joinColumn.getReferencedColumn());
    result.setReferencedColumn(joinColumn.getName());
    return result;
  }

  public static JoinTable invert(JoinTable joinTable) {
    if (joinTable == null) {
      return null;
    }

    var inverted = new JoinTable();
    inverted.setName(joinTable.getName());
    inverted.setJoinColumns(joinTable.getInverseJoinColumns());
    inverted.setInverseJoinColumns(joinTable.getJoinColumns());

    return inverted;
  }

  public static JoinColumn resolveReferencedField(JoinColumn joinColumn, PostgresObjectType objectType) {
    if (StringUtils.isNotBlank(joinColumn.getReferencedField())) {
      JoinColumn result = new JoinColumn();
      result.setName(joinColumn.getName());
      result.setReferencedColumn(objectType.getField(joinColumn.getReferencedField())
          .getColumn());
      return result;
    }

    return joinColumn;
  }

  public static boolean hasNestedReference(PostgresObjectField objectField) {
    if (!objectField.getJoinColumns()
        .isEmpty()) {
      return objectField.getJoinColumns()
          .stream()
          .anyMatch(JoinHelper::hasNestedReference);
    }
    return Optional.of(objectField)
        .filter(JoinHelper::hasNestedChild)
        .map(PostgresObjectField::getJoinTable)
        .stream()
        .anyMatch(JoinHelper::hasNestedReference);
  }

  private static boolean hasNestedReference(JoinTable joinTable) {
    return Optional.of(joinTable)
        .stream()
        .map(JoinTable::getInverseJoinColumns)
        .flatMap(Collection::stream)
        .anyMatch(JoinHelper::hasNestedReference);
  }

  private static boolean hasNestedReference(JoinColumn joinColumn) {
    return Optional.of(joinColumn)
        .map(JoinColumn::getReferencedField)
        .filter(field -> field.contains("."))
        .isPresent();
  }

  private static boolean hasNestedChild(PostgresObjectField objectField1) {
    return Optional.of(objectField1)
        .map(PostgresObjectField::getTargetType)
        .filter(ObjectType::isNested)
        .isPresent();
  }

  public static JoinTable resolveJoinTable(PostgresObjectType objectType, JoinTable joinTable) {
    if (joinTable == null) {
      return null;
    }

    var result = new JoinTable();
    result.setName(joinTable.getName());

    var joinColumns = joinTable.getJoinColumns()
        .stream()
        .map(joinColumn -> resolveReferencedField(joinColumn, objectType))
        .collect(Collectors.toList());

    result.setJoinColumns(joinColumns);

    var inverseJoinColumns = joinTable.getInverseJoinColumns()
        .stream()
        .map(JoinHelper::resolveJoinColumn)
        .collect(Collectors.toList());

    result.setInverseJoinColumns(inverseJoinColumns);

    return result;
  }

  public static List<JoinColumn> resolveJoinColumns(List<JoinColumn> joinColumns) {
    if (joinColumns == null || joinColumns.isEmpty()) {
      return joinColumns;
    }
    return joinColumns.stream()
        .map(JoinHelper::resolveJoinColumn)
        .collect(Collectors.toList());
  }

  private static JoinColumn resolveJoinColumn(JoinColumn joinColumn) {
    JoinColumn jc = new JoinColumn();
    jc.setName(joinColumn.getName());
    jc.setReferencedField(StringUtils.substringAfter(joinColumn.getReferencedField(), "."));
    jc.setReferencedColumn(joinColumn.getReferencedColumn());
    return jc;
  }

  public static Condition createJoinConditions(Table<Record> table, Table<Record> referencedTable,
      List<JoinColumn> joinColumns, PostgresObjectType objectType) {
    List<Condition> conditions = joinColumns.stream()
        .map(joinColumn -> column(table, joinColumn.getName()).equal(column(referencedTable, joinColumn, objectType)))
        .collect(Collectors.toList());

    return andCondition(conditions);
  }

  public static Condition andCondition(List<Condition> conditions) {
    if (conditions.size() == 1) {
      return conditions.get(0);
    }

    if (conditions.size() > 1) {
      return DSL.and(conditions);
    }

    throw illegalArgumentException("And condition called for empty condition list!");
  }
}
