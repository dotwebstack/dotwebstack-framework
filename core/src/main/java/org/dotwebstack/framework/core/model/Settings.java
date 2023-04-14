package org.dotwebstack.framework.core.model;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class Settings {

  @Valid
  private GraphQlSettings graphql;

  private int maxFilterDepth = 2;

  private int maxBatchSize = 250;

  private int maxBatchKeySize = 100;
}
