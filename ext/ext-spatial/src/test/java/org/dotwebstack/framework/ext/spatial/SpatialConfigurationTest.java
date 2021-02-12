package org.dotwebstack.framework.ext.spatial;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpatialConfigurationTest {

  private WiringFactory wiringFactory;

  @Mock
  private FieldWiringEnvironment environment;

  @BeforeEach
  void beforeAll() {

    wiringFactory = new SpatialConfiguration().wiringFactory();
  }

  @Test
  void providesDataFetcher_returnsTrue_forType() {
    // Arrange
    when(environment.getFieldType()).thenReturn(createOutputType(false));
    when(environment.getParentType()).thenReturn(createParentDefinition(GEOMETRY));
    when(environment.getFieldDefinition()).thenReturn(createFieldDefinition(TYPE));

    // Act
    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    // Assert
    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.TRUE));
  }

  @Test
  void providesDataFetcher_returnsTrue_forAsWkb() {
    // Arrange
    when(environment.getFieldType()).thenReturn(createOutputType(false));
    when(environment.getParentType()).thenReturn(createParentDefinition(GEOMETRY));
    when(environment.getFieldDefinition()).thenReturn(createFieldDefinition(AS_WKB));

    // Act
    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    // Assert
    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.TRUE));
  }

  @Test
  void providesDataFetcher_returnsTrue_forAsWkt() {
    // Arrange
    when(environment.getFieldType()).thenReturn(createOutputType(false));
    when(environment.getParentType()).thenReturn(createParentDefinition(GEOMETRY));
    when(environment.getFieldDefinition()).thenReturn(createFieldDefinition(AS_WKT));

    // Act
    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    // Assert
    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.TRUE));
  }

  @Test
  void providesDataFetcher_returnsFalse_unknownFieldName() {
    // Arrange
    when(environment.getFieldType()).thenReturn(createOutputType(false));
    when(environment.getParentType()).thenReturn(createParentDefinition(GEOMETRY));
    when(environment.getFieldDefinition()).thenReturn(createFieldDefinition("monkey"));

    // Act
    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    // Assert
    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.FALSE));
  }

  @Test
  void providesDataFetcher_returnsFalse_unknownParent() {
    // Arrange
    when(environment.getFieldType()).thenReturn(createOutputType(false));
    when(environment.getParentType()).thenReturn(createParentDefinition("monkey"));

    // Act
    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    // Assert
    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.FALSE));
  }

  @Test
  void providesDataFetcher_returnsFalse_forList() {
    // Arrange
    when(environment.getFieldType()).thenReturn(createOutputType(true));

    // Act
    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    // Assert
    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.FALSE));
  }

  @Test
  void getDataFetcher_returnsSpatialDataFetcher_always() {
    // Act
    DataFetcher<?> dataFetcher = wiringFactory.getDataFetcher(null);

    // Assert
    assertThat(dataFetcher, is(notNullValue()));
    assertThat(dataFetcher, instanceOf(SpatialDataFetcher.class));
  }

  private ObjectTypeDefinition createParentDefinition(String name) {
    return newObjectTypeDefinition().name(name)
        .build();
  }

  private FieldDefinition createFieldDefinition(String name) {
    return newFieldDefinition().name(name)
        .build();
  }

  private GraphQLOutputType createOutputType(boolean isList) {
    if (isList) {
      return mock(GraphQLList.class);
    }
    return mock(GraphQLOutputType.class);
  }
}
