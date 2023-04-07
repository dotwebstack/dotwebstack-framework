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

  protected boolean visible = true;

  protected String aggregationOf;

  protected String keyField;

  protected String valueFetcher;

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

  protected AbstractObjectField() {
    super();
  }

  protected AbstractObjectField(AbstractObjectField objectField) {
    super();
    this.name = objectField.getName();
    this.objectType = objectField.getObjectType();
    this.type = objectField.getType();
    this.keys = objectField.getKeys();
    this.isList = objectField.isList();
    this.nullable = objectField.isNullable();
    this.pageable = objectField.isPageable();
    this.visible = objectField.isVisible();
    this.aggregationOf = objectField.getAggregationOf();
    this.keyField = objectField.getKeyField();
    this.valueFetcher = objectField.getValueFetcher();
    this.enumeration = objectField.getEnumeration();
    this.aggregationOfType = objectField.getAggregationOfType();
    this.targetType = objectField.getTargetType();
    this.arguments = objectField.getArguments();
  }
}
