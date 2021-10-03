package org.dotwebstack.framework.core.backend.query;

import java.util.function.Function;

public interface FieldMapper<T> extends Function<T, Object> {
}
