package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Builder
@Data
public class AggregateFieldConfiguration {
    private final FieldConfiguration field;

    private String aggregateFunctionType; // TODO enum type? SUM, JOIN, AVG, MAX, MIN -> ENUM

    private boolean distinct;

    private String alias; // TODO even over nadenken omdat dit wil nodig gaat zijn

    private String type; // TODO enum String, Int, Float

    private String separator;
}
