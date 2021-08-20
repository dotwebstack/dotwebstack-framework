package org.dotwebstack.framework.service.openapi.response.oas;

import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public abstract class OasField {
  private OasType type;

  private boolean nillable;

  private boolean required;

  private boolean isDefault;

  private Object defaultValue;

  private boolean dwsTransient;

  private String dwsType;

  public OasField(OasType type, boolean nillable, boolean required) {
    this.type = type;
    this.nillable = nillable;
    this.required = required;
    this.isDefault = false;
    this.defaultValue = null;
    this.dwsTransient = false;
    this.dwsType = null;
  }

  public boolean isArray() {
    return type == OasType.ARRAY;
  }

  public boolean isScalar() {
    return type == OasType.SCALAR || type == OasType.SCALAR_EXPRESSION;
  }

  public boolean isEnvelopeObject() {
    return type == OasType.OBJECT && ((OasObjectField) this).isEnvelope;
  }

  public boolean hasDefault() {
    return this.defaultValue != null;
  }

  public boolean isTransient() {
    return isEnvelopeObject() || dwsTransient || hasDefault();
  }
}
