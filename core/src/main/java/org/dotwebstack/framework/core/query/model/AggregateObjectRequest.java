package org.dotwebstack.framework.core.query.model;


import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.backend.filter.GroupFilterCriteria;
import org.dotwebstack.framework.core.model.ObjectField;

@Builder
@Getter
public class AggregateObjectRequest {
  private final ObjectField objectField;

  private final List<AggregateField> aggregateFields;

  private final String key;

  private final GroupFilterCriteria filterCriteria;
}
