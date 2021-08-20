package org.dotwebstack.framework.service.openapi.response.oas;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OasOneOfField extends OasField {

  private List<OasField> content;

  public OasOneOfField(boolean nillable, boolean required, List<OasField> content) {
    super(OasType.ONE_OF, nillable, required);
    this.content = content;
  }
}
