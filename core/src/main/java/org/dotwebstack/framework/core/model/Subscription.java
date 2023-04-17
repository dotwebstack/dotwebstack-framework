package org.dotwebstack.framework.core.model;

import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Subscription {

  @NotBlank
  private String type;

  private List<String> keys = new ArrayList<>();

  private String context;
}
