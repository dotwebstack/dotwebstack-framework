package org.dotwebstack.framework.core.scalars;

import graphql.schema.Coercing;
import lombok.NonNull;

public interface CoreCoercing<T> extends Coercing<T, T> {

  boolean isCompatible(@NonNull String className);

}
