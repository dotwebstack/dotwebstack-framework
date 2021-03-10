package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.DataFetchingFieldSelectionSet;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;

@Builder
@Getter
public class QueryParameters {
  private final Collection<KeyCondition> keyConditions;

  private final DataFetchingFieldSelectionSet selectionSet;

  private final Page page;
}
