package org.dotwebstack.framework.core.graphql.scalars;

import com.google.common.collect.ImmutableList;
import graphql.schema.GraphQLScalarType;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
final class CoreScalarProvider implements ScalarProvider {

  @Override
  public Collection<GraphQLScalarType> getScalarTypes() {
    return ImmutableList.of(CoreScalars.DATE, CoreScalars.DATETIME);
  }

}
