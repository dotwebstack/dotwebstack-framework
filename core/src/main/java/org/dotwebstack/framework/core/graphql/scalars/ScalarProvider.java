package org.dotwebstack.framework.core.graphql.scalars;

import graphql.schema.GraphQLScalarType;
import java.util.Collection;

public interface ScalarProvider {

  Collection<GraphQLScalarType> getScalarTypes();

}
