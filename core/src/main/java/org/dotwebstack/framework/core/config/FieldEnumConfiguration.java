package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FieldEnumConfiguration {

  private String type;

  private List<Object> values = new ArrayList<>();
}
