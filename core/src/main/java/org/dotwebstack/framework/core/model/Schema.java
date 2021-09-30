package org.dotwebstack.framework.core.model;

import java.util.Map;
import lombok.Data;

@Data
public class Schema {

  private Map<String, ObjectType<? extends ObjectField>> objectTypes;
}
