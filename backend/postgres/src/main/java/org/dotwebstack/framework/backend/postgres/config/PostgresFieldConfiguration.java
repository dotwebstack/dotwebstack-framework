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

  private boolean isAggregate;

  public PostgresFieldConfiguration(String column, boolean isAggregate) {
    this.isAggregate = isAggregate;
    this.column = column;
  }

  public PostgresFieldConfiguration(List<JoinColumn> joinColumns) {
    this.joinColumns = joinColumns;
  }

  public PostgresFieldConfiguration(JoinTable joinTable) {
    this.joinTable = joinTable;
  }

  public boolean isScalar() {
    return getJoinColumns() == null && getJoinTable() == null && getMappedBy() == null && getAggregationOf() == null;
  }
}
