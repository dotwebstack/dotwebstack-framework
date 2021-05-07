package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionConfiguration {

  @NotBlank
  private String type;

  private List<KeyConfiguration> keys = new ArrayList<>();

}
