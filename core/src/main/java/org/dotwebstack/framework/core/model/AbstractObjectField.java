package org.dotwebstack.framework.core.model;

import lombok.Data;

@Data
public abstract class AbstractObjectField implements ObjectField {

  private String name;
}
