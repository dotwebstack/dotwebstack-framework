package org.dotwebstack.framework.core.query.model.origin;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.query.model.SortCriteria;

@Data
public class Sorting implements Origin {

  @EqualsAndHashCode.Exclude
  private final SortCriteria sortCriteria;

  @EqualsAndHashCode.Exclude
  private final Map<String, String> fieldPathAliasMap;
}
