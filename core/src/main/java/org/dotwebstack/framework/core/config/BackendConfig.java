package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "backend")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Rdf4jBackendConfig.class, name = "rdf4j"),

    @JsonSubTypes.Type(value = PostgresBackendConfig.class, name = "postgres") }
)
public abstract class BackendConfig {
  private List<String> keyFields;

  public List<String> getKeyFields() {
    return keyFields;
  }

  public void setKeyFields(List<String> keyFields) {
    this.keyFields = keyFields;
  }
}
