package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;

public final class DirectiveUtils {

  private DirectiveUtils() {
    throw new IllegalStateException(String.format("%s is not meant to be instantiated.", DirectiveUtils.class));
  }

  public static <T> T getArgument(@NonNull String argName, @NonNull GraphQLDirective directive,
      @NonNull Class<T> clazz) {
    final GraphQLArgument argument = directive.getArgument(argName);

    if (argument == null) {
      return null;
    }

    final Object argValue = argument.getValue();

    if (argValue == null) {
      return null;
    } else if (!clazz.isInstance(argValue)) {
      throw new InvalidConfigurationException("Argument type mismatch for '{}': expected[{}], but was [{}].", argName,
          clazz, argValue.getClass());
    } else {
      return clazz.cast(argValue);
    }
  }
}
