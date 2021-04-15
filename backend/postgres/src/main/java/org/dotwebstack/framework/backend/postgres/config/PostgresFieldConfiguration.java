package org.dotwebstack.framework.backend.postgres.config;

import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PostgresFieldConfiguration extends AbstractFieldConfiguration {

  @Valid
  private List<JoinColumn> joinColumns;

  @Valid
  private JoinTable joinTable;

  private String column;

  private boolean isNumeric = false;

  private boolean isText = false;

  private boolean isList = false;

  public boolean isScalar() {
    return getJoinColumns() == null && getJoinTable() == null && getMappedBy() == null && getAggregationOf() == null;
  }

  public boolean isAggregate() {
    return AggregateHelper.isAggregate(this);
  }

  public List<JoinColumn> findJoinColumns() {
    if (joinColumns != null) {
      return joinColumns;
    }

    if (joinTable != null) {
      return joinTable.getJoinColumns();
    }

    return List.of();
  }

  public List<JoinColumn> findInverseJoinColumns() {
    if (joinTable != null) {
      return joinTable.getInverseJoinColumns();
    }

    return List.of();
  }
}
