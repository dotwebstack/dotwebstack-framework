package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.GraphQLObjectType;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Builder
@Getter
public final class LoadEnvironment<T extends TypeConfiguration<? extends FieldConfiguration>> {

  private final T typeConfiguration;

  private final GraphQLObjectType objectType;
}
