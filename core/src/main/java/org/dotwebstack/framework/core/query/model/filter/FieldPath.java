package org.dotwebstack.framework.core.query.model.filter;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;

@Builder
@Data
public class FieldPath {
  private final @NonNull AbstractFieldConfiguration fieldConfiguration;

  private final FieldPath child;

  public boolean isLeaf() {
    return child == null;
  }

  public FieldPath getLeaf() {
    if (isLeaf()) {
      return this;
    }

    return child.getLeaf();
  }
}
