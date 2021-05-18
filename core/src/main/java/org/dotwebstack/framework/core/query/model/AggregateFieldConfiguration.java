package org.dotwebstack.framework.core.query.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.FieldConfiguration;

@Builder
@Data
public class AggregateFieldConfiguration {
    private final FieldConfiguration field;

    private String aggregateFunctionType; // TODO enum type? SUM, JOIN, AVG, MAX, MIN

    private boolean distinct;

    private String alias; // TODO even over nadenken omdat dit wil nodig gaat zijn

    private String type; // String, Int, Float
    private Class<?> scalarType;

    private Map<String, String> arguments; // separator TODO enum type?
}
