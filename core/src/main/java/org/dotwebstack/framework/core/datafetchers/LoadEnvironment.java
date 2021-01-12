package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.GraphQLObjectType;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;

@Builder
@Getter
public final class LoadEnvironment {

  private final AbstractTypeConfiguration<?> typeConfiguration;

  private final GraphQLObjectType objectType;

  private final List<String> selectedFields;
}
