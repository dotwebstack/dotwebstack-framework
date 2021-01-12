package org.dotwebstack.framework.core.datafetchers;

import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.config.TypeConfiguration;

@Builder
@Getter
public final class LoadEnvironment {

  private final TypeConfiguration<?> typeConfiguration;

  private final GraphQLObjectType objectType;

  private final List<SelectedField> selectedFields;
}
