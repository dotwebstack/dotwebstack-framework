package org.dotwebstack.framework.core.config;

import lombok.Data;

import java.util.List;

@Data
public class PostgresConfigField {
  private List<FieldItem> joinColumns;
  private String mappedBy;
}
