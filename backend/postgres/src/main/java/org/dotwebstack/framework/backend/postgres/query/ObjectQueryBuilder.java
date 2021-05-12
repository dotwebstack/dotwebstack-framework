package org.dotwebstack.framework.backend.postgres.query;

import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.query.model.KeyCriteria;

@Builder
@Getter
public class ObjectQueryBuilder {
  private final Collection<KeyCriteria> keyCriteria;
}
