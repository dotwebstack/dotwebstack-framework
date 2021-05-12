package org.dotwebstack.framework.core.query.model;

import lombok.Data;

@Data
public class KeyCriteria {
  private String field;
  private Object value;
}
