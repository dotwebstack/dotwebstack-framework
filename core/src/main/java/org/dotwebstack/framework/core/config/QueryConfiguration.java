package org.dotwebstack.framework.core.config;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.Data;

// TODO: wordt gebruikt voor queries en subscriptions, andere naam die dekkend is voor subscriptions
// en queries?
@Data
public class QueryConfiguration {

  @NotBlank
  private String type;

  private List<KeyConfiguration> keys = new ArrayList<>();

  private List<String> contextArguments = new ArrayList<>();

  private boolean nullable = false;

  private boolean list = false;

}
