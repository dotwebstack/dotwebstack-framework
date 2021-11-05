package org.dotwebstack.framework.core.model;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Query {

  @NotBlank
  private String type;

  private List<String> keys = new ArrayList<>();

  private boolean list = false;

  private boolean pageable = false;

  private String context;
}
