package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.GraphQLObjectType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class LoadEnvironment {

  private final GraphQLObjectType objectType;
}
