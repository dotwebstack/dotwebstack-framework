package org.dotwebstack.framework.core.model;

import lombok.Data;

@Data
public abstract class AbstractObjectField implements ObjectField {

  protected String name;

  protected String type;
}
