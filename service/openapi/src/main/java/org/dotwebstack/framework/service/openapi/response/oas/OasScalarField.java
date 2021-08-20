package org.dotwebstack.framework.service.openapi.response.oas;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class OasScalarField extends OasField {

  private String scalarType;

  public OasScalarField(boolean nillable, boolean required, String scalarType) {
    super(OasType.SCALAR, nillable, required);
    this.scalarType = scalarType;
  }
}
