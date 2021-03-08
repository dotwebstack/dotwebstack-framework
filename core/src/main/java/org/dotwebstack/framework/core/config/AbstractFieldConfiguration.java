package org.dotwebstack.framework.core.config;

import lombok.Data;

@Data
public abstract class AbstractFieldConfiguration implements FieldConfiguration {

  private String mappedBy;
  private String aggregationOf;
}
