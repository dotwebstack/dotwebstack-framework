package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryConfiguration {

  @NotBlank
  private String type;

  private List<KeyConfiguration> keys = new ArrayList<>();

  private boolean nullable = false;

  private boolean list = false;

  private boolean context = true;
}
