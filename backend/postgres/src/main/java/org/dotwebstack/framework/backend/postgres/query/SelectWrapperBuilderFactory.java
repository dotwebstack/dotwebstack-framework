package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isAggregate;

import lombok.AllArgsConstructor;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SelectWrapperBuilderFactory {

  private final DSLContext dslContext;

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final AggregateFieldFactory aggregateFieldFactory;

  public SelectWrapperBuilder getSelectWrapperBuilder(PostgresFieldConfiguration fieldConfiguration) {
    if (isAggregate(fieldConfiguration)) {
      return new AggregateSelectWrapperBuilder(dslContext, aggregateFieldFactory);
    }

    return getSelectWrapperBuilder();
  }

  public SelectWrapperBuilder getSelectWrapperBuilder() {
    return new DefaultSelectWrapperBuilder(dslContext, this, dotWebStackConfiguration);
  }

}
