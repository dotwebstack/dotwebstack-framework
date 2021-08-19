package org.dotwebstack.framework.service.openapi.response.oas;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class OasObjectField extends OasField {

  private Map<String, OasField> fields;

  boolean isEnvelope;

  private String includeExpression;

  public OasObjectField(boolean nillable, boolean required, Map<String, OasField> fields,
                        boolean isEnvelope, String includeExpression) {
    super(OasType.OBJECT, nillable, required);
    this.fields = fields;
    this.isEnvelope = isEnvelope;
    this.includeExpression = includeExpression;
  }

  public void add(String key, OasField value) {
    this.fields.put(key, value);
  }

  public Map<String, OasField> getFields() {
    return this.fields;
  }
}
