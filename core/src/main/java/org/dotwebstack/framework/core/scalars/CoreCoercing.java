package org.dotwebstack.framework.core.scalars;

import graphql.schema.Coercing;
import lombok.NonNull;

public interface BaseCoercing<T> extends Coercing<T, T> {

  boolean isCompatible(@NonNull String className);
}
