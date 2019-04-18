package org.dotwebstack.framework.core.graphql.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.dotwebstack.framework.core.InvalidConfigurationException;

@UtilityClass
public final class DirectiveUtils {

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
