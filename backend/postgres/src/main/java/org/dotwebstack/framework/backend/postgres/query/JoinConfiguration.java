package org.dotwebstack.framework.backend.postgres.query;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;


@Builder
@Getter
public class JoinConfiguration {

  @NonNull
  private PostgresObjectField objectField;

  private PostgresObjectField mappedBy;

  @Builder.Default
  private List<JoinColumn> joinColumns = new ArrayList<>();

  private JoinTable joinTable;

  @NonNull
  private PostgresObjectType objectType;

  @NonNull
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
