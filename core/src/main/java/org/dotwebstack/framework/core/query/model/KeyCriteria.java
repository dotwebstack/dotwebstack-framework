package org.dotwebstack.framework.core.query.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectField;

@Getter
@Builder
public class KeyCriteria {

  private List<ObjectField> fieldPath;

  private Object value;
}
