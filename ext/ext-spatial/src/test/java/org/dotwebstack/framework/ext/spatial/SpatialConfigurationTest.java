package org.dotwebstack.framework.ext.spatial;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.SRID;
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
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpatialConfigurationTest {

  private WiringFactory wiringFactory;

  @Mock
  private Spatial spatial;

  @Mock
  private FieldWiringEnvironment environment;

  @BeforeEach
  void beforeAll() {
    wiringFactory = new SpatialConfiguration(spatial, new TypeEnforcer()).wiringFactory();
  }

  @Test
  void providesDataFetcher_returnsTrue_forType() {
    providesDataFetcher_returnsTrue_for(TYPE, Boolean.TRUE);
  }

  @Test
  void providesDataFetcher_returnsTrue_forSrid() {
    providesDataFetcher_returnsTrue_for(SRID, Boolean.TRUE);
  }

  @Test
  void providesDataFetcher_returnsTrue_forAsWkb() {
    providesDataFetcher_returnsTrue_for(AS_WKB, Boolean.TRUE);
  }

  @Test
  void providesDataFetcher_returnsTrue_forAsWkt() {
    providesDataFetcher_returnsTrue_for(AS_WKT, Boolean.TRUE);
  }

  @Test
  void providesDataFetcher_returnsTrue_forAsGeoJson() {
    providesDataFetcher_returnsTrue_for(AS_GEOJSON, Boolean.TRUE);
  }

  @Test
  void providesDataFetcher_returnsFalse_unknownFieldName() {
    providesDataFetcher_returnsTrue_for("monkey", Boolean.FALSE);
  }

  private void providesDataFetcher_returnsTrue_for(String type, Boolean result) {
    when(environment.getFieldType()).thenReturn(createOutputType(false));
    when(environment.getParentType()).thenReturn(createParentDefinition(GEOMETRY));
    when(environment.getFieldDefinition()).thenReturn(createFieldDefinition(type));

    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(result));
  }

  @Test
  void providesDataFetcher_returnsFalse_unknownParent() {
    when(environment.getFieldType()).thenReturn(createOutputType(false));
    when(environment.getParentType()).thenReturn(createParentDefinition("monkey"));

    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.FALSE));
  }

  @Test
  void providesDataFetcher_returnsFalse_forList() {
    when(environment.getFieldType()).thenReturn(createOutputType(true));

    Boolean canProvideDataFetcher = wiringFactory.providesDataFetcher(environment);

    assertThat(canProvideDataFetcher, is(notNullValue()));
    assertThat(canProvideDataFetcher, is(Boolean.FALSE));
  }

  @Test
  void getDataFetcher_returnsSpatialDataFetcher_always() {
    DataFetcher<?> dataFetcher = wiringFactory.getDataFetcher(null);

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
