package org.dotwebstack.framework.backend.postgres.query;

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
    return Optional.of(objectField)
        .filter(JoinHelper::hasNestedChild)
        .map(PostgresObjectField::getJoinTable)
        .stream()
        .anyMatch(JoinHelper::hasNestedReference);
  }

  private static boolean hasNestedChild(PostgresObjectField objectField1) {
    return Optional.of(objectField1)
        .map(PostgresObjectField::getTargetType)
        .filter(ObjectType::isNested)
        .isPresent();
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
        .map(field -> field.contains("."))
        .isPresent();
  }

  public static JoinTable resolveJoinTable(PostgresObjectType objectType, JoinTable joinTable) {
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

  private static JoinColumn resolveJoinColumn(JoinColumn joinColumn) {
    JoinColumn jc = new JoinColumn();
    jc.setName(joinColumn.getName());
    jc.setReferencedField(StringUtils.substringAfter(joinColumn.getReferencedField(), "."));
    return jc;
  }
}
