package org.dotwebstack.framework.core.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public abstract class AbstractObjectField implements ObjectField {

  protected String name;

  protected String type;

  protected boolean isList = false;

  protected boolean nullable = false;

  private List<FieldArgument> arguments = new ArrayList<>();

}
