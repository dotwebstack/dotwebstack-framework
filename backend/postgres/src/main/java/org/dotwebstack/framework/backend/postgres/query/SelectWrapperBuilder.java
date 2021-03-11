package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.DataFetchingFieldSelectionSet;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;

public interface SelectWrapperBuilder {
  SelectWrapper build(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration, String fieldPathPrefix,
      JoinTable parentJoinTable, DataFetchingFieldSelectionSet selectionSet);
}
