package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;

public final class DirectiveUtils {

  private DirectiveUtils() {}

  public static <T> T getArgument(@NonNull GraphQLFieldDefinition fieldDefinition, @NonNull String directiveName,
      @NonNull String argumentName, @NonNull Class<T> clazz) {
    GraphQLDirective directive = fieldDefinition.getDirective(directiveName);

    if (Objects.isNull(directive)) {
      return null;
    }
    return getArgument(argumentName, directive, clazz);
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
