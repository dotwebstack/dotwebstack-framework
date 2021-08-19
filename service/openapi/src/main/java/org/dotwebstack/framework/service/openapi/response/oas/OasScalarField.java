package org.dotwebstack.framework.service.openapi.response.oas;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OasScalarField extends OasField {

  private String scalarType;

  public OasScalarField(boolean nillable, boolean required, String scalarType) {
    super(OasType.SCALAR, nillable, required);
    this.scalarType = scalarType;
  }
}
