package org.dotwebstack.framework.core.config;

import lombok.Data;

import java.util.Map;

@Data
public class Rdf4jBackendConfig extends BackendConfig {
  private String nodeShape;
  private Map<String,Rdf4jConfigField> fields;
}
