package org.dotwebstack.framework.backend.postgres.query.objectQuery;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import lombok.Data;

@Data
public class ObjectQueryContext {

    private final AtomicInteger selectCounter = new AtomicInteger();

    private AtomicReference<String> checkNullAlias = new AtomicReference<>();

    Map<String, Function<Map<String, Object>, Object>> assembleFns = new HashMap<>();

    public String newSelectAlias() {
        return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
    }
}
