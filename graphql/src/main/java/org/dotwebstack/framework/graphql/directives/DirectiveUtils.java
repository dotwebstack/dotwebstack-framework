package org.dotwebstack.framework.graphql.directives;

import graphql.schema.GraphQLDirective;
import org.dotwebstack.framework.core.InvalidConfigurationException;

public final class DirectiveUtils {

  private DirectiveUtils() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", DirectiveUtils.class));
  }

  public static String getStringArgument(GraphQLDirective directive, String argName) {
    Object argValue = directive.getArgument(argName).getValue();

    if (!(argValue instanceof String)) {
      throw new InvalidConfigurationException(
          String.format("Argument '%s' is not a string.", argName));
    }

    return (String) argValue;
  }

}
