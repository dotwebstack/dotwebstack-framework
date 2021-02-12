package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY_TYPE;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.LINESTRING;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTILINESTRING;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTIPOINT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTIPOLYGON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.POINT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.POLYGON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class SpatialConfigurerTest {

  private SpatialConfigurer spatialConfigurer;

  @Mock
  private TypeDefinitionRegistry dataFetchingEnvironment;

  @BeforeEach
  void beforeAll() {
    spatialConfigurer = new SpatialConfigurer();
    dataFetchingEnvironment = new TypeDefinitionRegistry();
  }

  @Test
  void configureTypeDefinitionRegistry_addEnumDef_always() {
    // Arrange
    List<String> enumValues = List.of(POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON);

    // Act
    spatialConfigurer.configureTypeDefinitionRegistry(dataFetchingEnvironment);

    // Assert
    Optional<TypeDefinition> optional = dataFetchingEnvironment.getType(GEOMETRY_TYPE);
    assertThat(optional, is(notNullValue()));
    assertThat(optional.get(), is(notNullValue()));
    assertThat(optional.get(), instanceOf(EnumTypeDefinition.class));
    EnumTypeDefinition enumTypeDefinition = (EnumTypeDefinition) optional.get();
    for (EnumValueDefinition enumValue : enumTypeDefinition.getEnumValueDefinitions()) {

      assertThat(enumValues.contains(enumValue.getName()), is(Boolean.TRUE));
    }
  }

  @Test
  void configureTypeDefinitionRegistry_addGeometryDef_always() {
    // Arrange
    List<String> fieldNames = List.of(TYPE, AS_WKB, AS_WKT);

    // Act
    spatialConfigurer.configureTypeDefinitionRegistry(dataFetchingEnvironment);

    // Assert
    Optional<TypeDefinition> optional = dataFetchingEnvironment.getType(GEOMETRY);
    assertThat(optional, is(notNullValue()));
    assertThat(optional.get(), is(notNullValue()));
    assertThat(optional.get(), instanceOf(ObjectTypeDefinition.class));
    ObjectTypeDefinition enumTypeDefinition = (ObjectTypeDefinition) optional.get();
    for (FieldDefinition field : enumTypeDefinition.getFieldDefinitions()) {

      assertThat(fieldNames.contains(field.getName()), is(Boolean.TRUE));
    }
  }
}
