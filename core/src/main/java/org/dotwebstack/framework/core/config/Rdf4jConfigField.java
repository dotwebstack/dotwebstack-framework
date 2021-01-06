package org.dotwebstack.framework.core.config;

import lombok.Data;

import java.util.List;

@Data
public class Rdf4jConfigField {
  private List<FieldItem> joinProperties;
}
