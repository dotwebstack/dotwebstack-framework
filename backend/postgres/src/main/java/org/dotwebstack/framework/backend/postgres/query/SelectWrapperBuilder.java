package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.SelectedField;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;

public interface SelectWrapperBuilder {
  SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration, String fieldPathPrefix,
      JoinTable parentJoinTable);

  SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      JoinTable parentJoinTable, Map<String, SelectedField> selectedFields);
}
