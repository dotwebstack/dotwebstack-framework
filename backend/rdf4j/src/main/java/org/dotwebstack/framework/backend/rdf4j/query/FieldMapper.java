package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.function.Function;
import org.eclipse.rdf4j.query.BindingSet;

public interface FieldMapper<T> extends Function<BindingSet, T> {
}