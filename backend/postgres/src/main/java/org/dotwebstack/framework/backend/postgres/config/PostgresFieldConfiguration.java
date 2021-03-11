package org.dotwebstack.framework.backend.postgres.config;

import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PostgresFieldConfiguration extends AbstractFieldConfiguration {

  @Valid
  private List<JoinColumn> joinColumns;

  @Valid
  private JoinTable joinTable;

  private String column;

  private Boolean isNumeric = false;

  public boolean isScalar() {
    return getJoinColumns() == null && getJoinTable() == null && getMappedBy() == null && getAggregationOf() == null;
  }
}
