package org.dotwebstack.framework.core.datafetchers.paging;

import static java.util.Optional.of;
import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.BackendExecutionStepInfo;
import org.dotwebstack.framework.core.backend.BackendLoader;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.backend.BackendRequestFactory;
import org.dotwebstack.framework.core.datafetchers.CounterDataFetcher;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class CounterDataFetcherWiringFactory implements WiringFactory {

  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return environment.getFieldDefinition()
        .getType()
        .getAdditionalData()
        .containsKey(GraphQlConstants.IS_COUNTER_TYPE + "test");
  }

  @Override
  public DataFetcher<?> getDataFetcher(FieldWiringEnvironment environment) {
    var typeName = getTypeName(environment.getFieldType()).orElseThrow();

    //    var objectType = of(typeName).flatMap(schema::getObjectType)
    //        .orElseThrow();
    //    var backendLoader = backendModule.getBackendLoaderFactory()
    //        .create(objectType);

    return new CounterDataFetcher();
  }
}
