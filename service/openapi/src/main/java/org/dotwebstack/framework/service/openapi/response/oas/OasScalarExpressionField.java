package org.dotwebstack.framework.service.openapi.response.oas;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OasScalarExpressionField extends OasField {
  private String scalarType;

  private String expression;

  private String fallbackValue;

  public OasScalarExpressionField(boolean nillable, boolean required, String scalarType, String expression,
      String fallBackValue) {
    super(OasType.SCALAR_EXPRESSION, nillable, required);
    this.scalarType = scalarType;
    this.expression = expression;
    this.fallbackValue = fallBackValue;
  }
}
