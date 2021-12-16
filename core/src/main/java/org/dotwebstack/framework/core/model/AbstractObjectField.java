package org.dotwebstack.framework.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;

@Data
public abstract class AbstractObjectField implements ObjectField {

  @Valid
  protected ObjectType<? extends ObjectField> objectType;

  protected String name;

  protected String type;

  protected List<String> keys = new ArrayList<>();

  protected boolean isList = false;

  protected boolean nullable = false;

  protected boolean pageable = false;

  protected String aggregationOf;

  protected String keyField;

  @JsonProperty("enum")
  protected FieldEnumConfiguration enumeration;

  @Valid
  protected ObjectType<? extends ObjectField> aggregationOfType;

  private ObjectType<? extends ObjectField> targetType;

  @Valid
  protected List<FieldArgument> arguments = new ArrayList<>();

  public boolean isEnumeration() {
    return enumeration != null;
  }
}
