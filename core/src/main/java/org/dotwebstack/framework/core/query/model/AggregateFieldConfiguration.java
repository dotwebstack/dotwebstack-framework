package org.dotwebstack.framework.core.query.model;

import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Builder
@Data
public class AggregateFieldConfiguration {
    private final FieldConfiguration field;

    private AggregateFunctionType aggregateFunctionType;

    private boolean distinct;

    private String alias; // TODO even over nadenken of we dit nodig hebben

    private ScalarType type;

    private String separator;
}
