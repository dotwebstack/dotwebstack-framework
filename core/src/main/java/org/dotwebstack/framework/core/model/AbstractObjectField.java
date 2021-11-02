package org.dotwebstack.framework.core.model;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.Data;

@Data
public abstract class AbstractObjectField implements ObjectField {

  @Valid
  protected ObjectType<? extends ObjectField> objectType;

  protected String name;

  protected String type;

  protected List<String> keys = new ArrayList<>();

  protected boolean isList = false;

  protected boolean nullable = false;

  protected String aggregationOf;

  @Valid
  protected ObjectType<? extends ObjectField> aggregationOfType;

  private ObjectType<? extends ObjectField> targetType;

  @Valid
  protected List<FieldArgument> arguments = new ArrayList<>();
}
