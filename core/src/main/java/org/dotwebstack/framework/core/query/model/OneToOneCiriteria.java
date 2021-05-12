package org.dotwebstack.framework.core.query.model;

import lombok.Data;
import org.dotwebstack.framework.core.config.TypeConfiguration;

import java.util.List;

@Data
public class OneToOneCiriteria {
  private TypeConfiguration<?> typeConfiguration;
  private String source;
  private List<String> fields;

}
