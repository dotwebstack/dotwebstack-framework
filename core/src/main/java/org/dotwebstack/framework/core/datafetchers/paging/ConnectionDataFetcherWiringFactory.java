package org.dotwebstack.framework.core.datafetchers.paging;

import static org.dotwebstack.framework.core.helpers.TypeHelper.isConnectionType;

import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(DotWebStackConfiguration.class)
public class ConnectionDataFetcherWiringFactory implements WiringFactory {

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  public ConnectionDataFetcherWiringFactory(TypeDefinitionRegistry typeDefinitionRegistry) {
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return isConnectionType(typeDefinitionRegistry, environment.getFieldDefinition()
        .getType());
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    return new ConnectionDataFetcher();
  }
}
