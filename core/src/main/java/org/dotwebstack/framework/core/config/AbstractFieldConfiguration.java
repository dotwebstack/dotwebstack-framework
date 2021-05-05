package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public abstract class AbstractFieldConfiguration implements FieldConfiguration {

  private String type;

  private boolean nullable = false;

  private boolean list = false;

  private String mappedBy;

  private String aggregationOf;

  private List<FieldArgumentConfiguration> arguments = new ArrayList<>();
}
