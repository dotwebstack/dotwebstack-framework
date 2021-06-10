package org.dotwebstack.framework.core.datafetchers.paging;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.TYPE_SUFFIX_NAME;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.Optional;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.springframework.stereotype.Component;

@Component
public class ConnectionDataFetcherWiringFactory implements WiringFactory {

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return hasTypeNameWithConnection(environment) && hasConnectionFields(environment);
  }

  private boolean hasTypeNameWithConnection(FieldWiringEnvironment environment) {
    return TypeHelper.getTypeName(environment.getFieldDefinition()
        .getType())
        .endsWith(TYPE_SUFFIX_NAME);
  }

  private boolean hasConnectionFields(FieldWiringEnvironment environment) {
    return Optional.ofNullable(environment.getFieldType())
        .filter(GraphQLNonNull.class::isInstance)
        .map(GraphQLNonNull.class::cast)
        .map(GraphQLNonNull::getWrappedType)
        .filter(GraphQLObjectType.class::isInstance)
        .map(GraphQLObjectType.class::cast)
        .stream()
        .anyMatch(graphQLObjectType -> graphQLObjectType.getFieldDefinition(PagingConstants.NODES_FIELD_NAME) != null
            && graphQLObjectType.getFieldDefinition(PagingConstants.OFFSET_FIELD_NAME) != null);
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    return new ConnectionDataFetcher();
  }

}
