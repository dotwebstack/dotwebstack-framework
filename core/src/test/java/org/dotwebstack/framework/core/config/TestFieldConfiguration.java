package org.dotwebstack.framework.core.config;

import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;

@Data
public class TestFieldConfiguration extends AbstractFieldConfiguration {
  @Valid
  private List<TestJoinColumn> joinColumns;

  @Valid
  private TestJoinTable joinTable;

  private String column;

  private boolean isNumeric = false;

  private boolean isText = false;

  private boolean isList = false;

  @Override
  public String getType() {
    if (isAggregate()) {
      return AggregateConstants.AGGREGATE_TYPE;
    }
    return super.getType();
  }

  public boolean isAggregate() {
    return AggregateHelper.isAggregate(this);
  }
}
