package org.dotwebstack.framework.backend.rdf4j.config;

import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
public class Rdf4jFieldConfiguration extends FieldConfiguration {

  @Valid
  private final List<JoinColumn> joinProperties;
}
