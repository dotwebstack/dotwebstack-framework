package org.dotwebstack.framework.core.datafetchers.paging;

import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class ConnectionDataFetcherWiringFactory implements WiringFactory {

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return environment.getFieldDefinition()
        .getType()
        .getAdditionalData()
        .containsKey(GraphQlConstants.IS_CONNECTION_TYPE);
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    return new ConnectionDataFetcher();
  }
}
