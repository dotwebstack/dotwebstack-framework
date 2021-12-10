package org.dotwebstack.framework.backend.postgres.query;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;


@Builder
@Getter
public class JoinConfiguration {
  private PostgresObjectField objectField;

  private PostgresObjectField mappedBy;

  private List<JoinColumn> joinColumns;

  private JoinTable joinTable;

  private PostgresObjectType objectType;

  private PostgresObjectType targetType;

  public static JoinConfiguration toJoinConfiguration(PostgresObjectField objectField) {
    return JoinConfiguration.builder()
        .objectField(objectField)
        .targetType((PostgresObjectType) objectField.getTargetType())
        .objectType((PostgresObjectType) objectField.getObjectType())
        .mappedBy(objectField.getMappedByObjectField())
        .joinTable(objectField.getJoinTable())
        .joinColumns(objectField.getJoinColumns())
        .build();
  }

  public static JoinConfiguration toJoinConfiguration(PostgresObjectField objectField,
      PostgresJoinCondition joinCondition) {
    JoinTable joinTable;
    if (joinCondition != null && joinCondition.getJoinTable() != null) {
      joinTable = joinCondition.getJoinTable();
    } else {
      joinTable = objectField.getJoinTable();
    }

    return JoinConfiguration.builder()
        .objectField(objectField)
        .targetType((PostgresObjectType) objectField.getTargetType())
        .objectType((PostgresObjectType) objectField.getObjectType())
        .mappedBy(objectField.getMappedByObjectField())
        .joinTable(joinTable)
        .joinColumns(objectField.getJoinColumns())
        .build();
  }
}
