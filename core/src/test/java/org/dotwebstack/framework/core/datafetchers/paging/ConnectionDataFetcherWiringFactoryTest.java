package org.dotwebstack.framework.core.datafetchers.paging;

import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.schema.idl.FieldWiringEnvironment;
import java.util.Map;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConnectionDataFetcherWiringFactoryTest {

  private ConnectionDataFetcherWiringFactory wiringFactory;

  @BeforeEach
  void beforeEach() {
    wiringFactory = new ConnectionDataFetcherWiringFactory();
  }

  @Test
  void providesDataFetcher_returnsTrue_forConnectionObject() {
    var fieldWiringEnvironment = mock(FieldWiringEnvironment.class);

    var fieldDefinition = mock(FieldDefinition.class);
    when(fieldWiringEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    var type = newTypeName("testType").build();
    when(fieldDefinition.getType()).thenReturn(type);

    var typeDefinition =
        newObjectTypeDefinition().additionalData(Map.of(GraphQlConstants.IS_CONNECTION_TYPE, Boolean.TRUE.toString()))
            .build();

    assertThat(wiringFactory.providesDataFetcher(fieldWiringEnvironment), CoreMatchers.equalTo(Boolean.TRUE));
  }

  @Test
  void providesDataFetcher_returnsTrue_forConnectionObjectWithExplicitFalse() {
    var fieldWiringEnvironment = mock(FieldWiringEnvironment.class);

    var fieldDefinition = mock(FieldDefinition.class);
    when(fieldWiringEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    var type = newTypeName("testType").build();
    when(fieldDefinition.getType()).thenReturn(type);

    var typeDefinition =
        newObjectTypeDefinition().additionalData(Map.of(GraphQlConstants.IS_CONNECTION_TYPE, Boolean.FALSE.toString()))
            .build();

    assertThat(wiringFactory.providesDataFetcher(fieldWiringEnvironment), CoreMatchers.equalTo(Boolean.FALSE));
  }

  @Test
  void providesDataFetcher_returnsFalse_forDefaultObject() {
    var fieldWiringEnvironment = mock(FieldWiringEnvironment.class);

    var fieldDefinition = mock(FieldDefinition.class);
    when(fieldWiringEnvironment.getFieldDefinition()).thenReturn(fieldDefinition);

    var type = newTypeName("testType").build();
    when(fieldDefinition.getType()).thenReturn(type);

    var typeDefinition = newObjectTypeDefinition().build();

    assertThat(wiringFactory.providesDataFetcher(fieldWiringEnvironment), CoreMatchers.equalTo(Boolean.FALSE));
  }

  @Test
  void getDataFetcher_returnsConnectionDataFetcher_forDefault() {
    var fieldWiringEnvironment = mock(FieldWiringEnvironment.class);

    assertThat(wiringFactory.getDataFetcher(fieldWiringEnvironment),
        CoreMatchers.instanceOf(ConnectionDataFetcher.class));
  }
}
