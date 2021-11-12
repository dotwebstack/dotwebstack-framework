package org.dotwebstack.framework.backend.postgres.query;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;

public class JoinHelper {

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

  public static JoinTable resolveJoinTable(PostgresObjectType objectType, JoinTable joinTable) {
    var newJoinTable = new JoinTable();
    newJoinTable.setName(joinTable.getName());
    newJoinTable.setJoinColumns(joinTable.getJoinColumns()
        .stream()
        .map(joinColumn -> resolveReferencedField(joinColumn, objectType))
        .collect(Collectors.toList()));
    newJoinTable.setInverseJoinColumns(joinTable.getInverseJoinColumns()
        .stream()
        .map(joinColumn -> {
          JoinColumn jc = new JoinColumn();
          jc.setName(joinColumn.getName());
          jc.setReferencedField(StringUtils.substringAfter(joinColumn.getReferencedField(), "."));
          return jc;
        })
        .collect(Collectors.toList()));

    return newJoinTable;
  }
}
