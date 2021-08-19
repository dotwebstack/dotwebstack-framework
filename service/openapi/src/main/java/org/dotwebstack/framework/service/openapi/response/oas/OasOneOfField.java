package org.dotwebstack.framework.service.openapi.response.oas;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OasOneOfField extends OasField {

  private List<OasField> content;

  public OasOneOfField(boolean nillable, boolean required, List<OasField> content) {
    super(OasType.ONE_OF, nillable, required);
    this.content = content;
  }
}
