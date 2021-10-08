package org.dotwebstack.framework.core.backend.filter;

import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

@Builder
@Getter
public class ObjectFieldPath {
  private ObjectField objectField;

  private ObjectType<?> objectType;
}
