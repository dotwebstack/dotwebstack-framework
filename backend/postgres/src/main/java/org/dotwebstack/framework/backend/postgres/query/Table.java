package org.dotwebstack.framework.backend.postgres.query;

public interface Table {

  String getAlias();

  String toSql(VarBinder varBinder);

  default Column newColumn(String name) {
    return Column.builder()
        .table(this)
        .name(name)
        .build();
  }
}
