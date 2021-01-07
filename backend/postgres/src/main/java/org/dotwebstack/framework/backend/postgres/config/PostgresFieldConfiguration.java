package org.dotwebstack.framework.backend.postgres.config;

import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresFieldConfiguration extends FieldConfiguration {

  @Valid
  private List<JoinColumn> joinColumns;
}
