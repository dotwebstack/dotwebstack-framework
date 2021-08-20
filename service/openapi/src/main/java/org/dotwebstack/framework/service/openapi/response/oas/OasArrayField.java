package org.dotwebstack.framework.service.openapi.response.oas;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class OasArrayField extends OasField {
  private OasField content;

  public OasArrayField(boolean nillable, boolean required, OasField content) {
    super(OasType.ARRAY, nillable, required);
    this.content = content;
  }
}
