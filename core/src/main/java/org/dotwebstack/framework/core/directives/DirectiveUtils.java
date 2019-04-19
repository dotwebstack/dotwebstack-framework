package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;

public final class DirectiveUtils {

  private DirectiveUtils() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", DirectiveUtils.class));
  }

  public static String getStringArgument(@NonNull String argName,
      @NonNull GraphQLDirective directive) {
    GraphQLArgument argument = directive.getArgument(argName);

    if (argument == null) {
      return null;
    }

    Object argValue = directive.getArgument(argName).getValue();

    if (argValue == null) {
      return null;
    }

    if (!(argValue instanceof String)) {
      throw new InvalidConfigurationException(
          String.format("Argument '%s' is not a string.", argName));
    }

    return (String) argValue;
  }

}
