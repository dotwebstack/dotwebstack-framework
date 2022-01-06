package org.dotwebstack.framework.core.query.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectField;

@Getter
@Builder
public class KeyCriteria {

  private Map<List<ObjectField>, Object> values;
}
