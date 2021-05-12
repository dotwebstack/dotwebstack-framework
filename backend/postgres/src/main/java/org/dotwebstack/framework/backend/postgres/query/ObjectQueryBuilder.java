package org.dotwebstack.framework.backend.postgres.query;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.query.model.KeyCriteria;

import java.util.Collection;

@Builder
@Getter
public class ObjectQueryBuilder {
  private final Collection<KeyCriteria> keyCriteria;
}
