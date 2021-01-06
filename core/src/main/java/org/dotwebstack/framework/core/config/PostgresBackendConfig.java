package org.dotwebstack.framework.core.config;

import lombok.Data;

import java.util.Map;

@Data
public class PostgresBackendConfig extends BackendConfig {
  private String table;
  private Map<String,PostgresConfigField> fields;
}
