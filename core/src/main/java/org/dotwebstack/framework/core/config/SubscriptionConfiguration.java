package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

// TODO: wordt gebruikt voor queries en subscriptions, andere naam die dekkend is voor subscriptions
// en queries?
@Data
public class SubscriptionConfiguration {

  @NotBlank
  private String type;

  private List<KeyConfiguration> keys = new ArrayList<>();

}
