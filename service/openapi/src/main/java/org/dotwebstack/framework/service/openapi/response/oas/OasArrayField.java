package org.dotwebstack.framework.service.openapi.response.oas;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OasArrayField extends OasField {
  private OasField content;

  public OasArrayField(boolean nillable, boolean required, OasField content) {
    super(OasType.ARRAY, nillable, required);
    this.content = content;
  }
}
