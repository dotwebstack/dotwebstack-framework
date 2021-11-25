package org.dotwebstack.framework.ext.spatial;

import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_GEOJSON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKB;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.AS_WKT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRYCOLLECTION;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY_FILTER;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.GEOMETRY_TYPE;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.LINESTRING;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTILINESTRING;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTIPOINT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.MULTIPOLYGON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.POINT;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.POLYGON;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.SRID;
import static org.dotwebstack.framework.ext.spatial.SpatialConstants.TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpatialConfigurerTest {

  private SpatialConfigurer spatialConfigurer;

  private TypeDefinitionRegistry dataFetchingEnvironment;

  @BeforeEach
  void beforeAll() {
    spatialConfigurer = new SpatialConfigurer();
    dataFetchingEnvironment = new TypeDefinitionRegistry();
  }

  @Test
  @SuppressWarnings("rawtypes")
  void configureTypeDefinitionRegistry_addEnumDef_always() {
    spatialConfigurer.configureTypeDefinitionRegistry(dataFetchingEnvironment);

    Optional<TypeDefinition> optional = dataFetchingEnvironment.getType(GEOMETRY_TYPE);
    assertThat(optional.isPresent(), is(true));
    assertThat(optional.get(), instanceOf(EnumTypeDefinition.class));

    List<String> enumValues =
        List.of(POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON, GEOMETRYCOLLECTION);
    List<EnumValueDefinition> enumValueDefs = ((EnumTypeDefinition) optional.get()).getEnumValueDefinitions();
    assertThat(enumValueDefs.size(), equalTo(enumValues.size()));

    for (EnumValueDefinition enumValue : enumValueDefs) {
      assertThat(enumValues.contains(enumValue.getName()), is(Boolean.TRUE));
    }
  }

  @Test
  @SuppressWarnings("rawtypes")
  void configureTypeDefinitionRegistry_addGeometryDef_always() {
    spatialConfigurer.configureTypeDefinitionRegistry(dataFetchingEnvironment);

    Optional<TypeDefinition> optional = dataFetchingEnvironment.getType(GEOMETRY);
    assertThat(optional.isPresent(), is(true));
    assertThat(optional.get(), instanceOf(ObjectTypeDefinition.class));

    List<String> fieldNames = List.of(SRID, TYPE, AS_WKB, AS_WKT, AS_GEOJSON);
    List<FieldDefinition> geometryFieldDefs = ((ObjectTypeDefinition) optional.get()).getFieldDefinitions();
    assertThat(geometryFieldDefs.size(), equalTo(fieldNames.size()));

    for (FieldDefinition field : geometryFieldDefs) {
      assertThat(fieldNames.contains(field.getName()), is(Boolean.TRUE));
      assertThat(field.getType(), instanceOf(NonNullType.class));
    }
  }

  @Test
  void configureFieldFilterMapping_addGeometryFilter_always() {
    Map<String, String> fieldFilterMap = new HashMap<>();

    spatialConfigurer.configureFieldFilterMapping(fieldFilterMap);

    assertThat(fieldFilterMap, allOf(hasEntry(is(GEOMETRY), is(GEOMETRY_FILTER))));
  }
}
