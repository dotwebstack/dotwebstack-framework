package org.dotwebstack.framework.param;

import lombok.Getter;
import lombok.Setter;


public abstract class AbstractPropertyShape<T> implements PropertyShape<T> {

  @Getter
  @Setter
  protected T defaultValue;
}
