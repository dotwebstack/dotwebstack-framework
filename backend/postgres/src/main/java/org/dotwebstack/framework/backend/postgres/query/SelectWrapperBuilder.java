package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;

public interface SelectWrapperBuilder {
  SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration, String fieldPathPrefix,
      JoinTable parentJoinTable, DataFetchingFieldSelectionSet selectionSet);

  SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      JoinTable parentJoinTable, DataFetchingFieldSelectionSet selectionSet, Map<String, SelectedField> selectedFields);
}
