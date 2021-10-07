package org.dotwebstack.framework.ext.spatial;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterCriteriaParserBaseTest;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.NotFilterCriteria;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeometryFilterCriteriaParserTest extends FilterCriteriaParserBaseTest {

  @Mock
  private SpatialConfigurationProperties spatialConfigurationProperties;

  private GeometryFilterCriteriaParser parser;

  @BeforeAll
  public static void init() {
    new SpatialConfigurer().configureTypeDefinitionRegistry(dataFetchingEnvironment);
  }

  @BeforeEach
  public void beforeEach() {
    parser = new GeometryFilterCriteriaParser(spatialConfigurationProperties);
  }

  @ParameterizedTest
  @CsvSource({SpatialConstants.GEOMETRY_FILTER})
  void supports_returnsTrue_forSupportedScalar(String typeName) {
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, typeName);

    boolean result = parser.supports(inputObjectField);

    assertThat(result, is(true));
  }

  @Test
  void supports_returnsFalse_forUnsupportedScalar() {
    GraphQLInputObjectField inputObjectField = createInputObjectField(FIELD_TEST, Scalars.GraphQLBoolean);

    boolean result = parser.supports(inputObjectField);

    assertThat(result, is(false));
  }

  @ParameterizedTest
  @CsvSource({"within", "contains", "intersects"})
  void parse_returnsFilterCriterias_forWktGeoFilters(String geometryOperator) {
    var wkt = "POINT (194936.73 470973.96)";
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(geometryOperator, Map.of(SpatialConstants.FROM_WKT, wkt)));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(GeometryFilterCriteria.class));
    assertThat(((GeometryFilterCriteria) result.get(0)).getFilterOperator()
        .name()
        .toLowerCase(), is(geometryOperator));
    assertThat(((GeometryFilterCriteria) result.get(0)).getGeometry()
        .toString(), is(wkt));
  }

  @ParameterizedTest
  @CsvSource({"within", "contains", "intersects"})
  void parse_returnsFilterCriterias_forWktNotGeoFilters(String geometryOperator) {
    var wkt = "POINT (194936.73 470973.96)";
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST,
        Map.of(FilterConstants.NOT_FIELD, Map.of(geometryOperator, Map.of(SpatialConstants.FROM_WKT, wkt))));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(NotFilterCriteria.class));

    var notFilterCriteria = (NotFilterCriteria) result.get(0);

    assertThat(((GeometryFilterCriteria) notFilterCriteria.getFilterCriteria()).getFilterOperator()
        .name()
        .toLowerCase(), is(geometryOperator));
    assertThat(((GeometryFilterCriteria) notFilterCriteria.getFilterCriteria()).getGeometry()
        .toString(), is(wkt));
  }

  @ParameterizedTest
  @CsvSource({"within", "contains", "intersects"})
  void parse_returnsFilterCriterias_forWkbGeoFilters(String geometryOperator) {
    var wkb = "ACAAAAEAAHFAQQfLxdcKPXFBHL731wo9cQ==";
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(geometryOperator, Map.of(SpatialConstants.FROM_WKB, wkb)));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(GeometryFilterCriteria.class));
    assertThat(((GeometryFilterCriteria) result.get(0)).getFilterOperator()
        .name()
        .toLowerCase(), is(geometryOperator));
    assertThat(((GeometryFilterCriteria) result.get(0)).getGeometry()
        .toString(), is("POINT (194936.73 470973.96)"));
  }

  @ParameterizedTest
  @CsvSource({"within", "contains", "intersects"})
  void parse_returnsFilterCriterias_forWkbNotGeoFilters(String geometryOperator) {
    var wkb = "ACAAAAEAAHFAQQfLxdcKPXFBHL731wo9cQ==";
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST,
        Map.of(FilterConstants.NOT_FIELD, Map.of(geometryOperator, Map.of(SpatialConstants.FROM_WKB, wkb))));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(NotFilterCriteria.class));

    var notFilterCriteria = (NotFilterCriteria) result.get(0);

    assertThat(((GeometryFilterCriteria) notFilterCriteria.getFilterCriteria()).getFilterOperator()
        .name()
        .toLowerCase(), is(geometryOperator));
    assertThat(((GeometryFilterCriteria) notFilterCriteria.getFilterCriteria()).getGeometry()
        .toString(), is("POINT (194936.73 470973.96)"));
  }

  @ParameterizedTest
  @CsvSource({"within", "contains", "intersects"})
  void parse_returnsFilterCriterias_forGeoJsonGeoFilters(String geometryOperator) {
    var geoJson = "{\n" + "  \"type\": \"Point\",\n" + "  \"coordinates\": [\n" + "    194936.73,\n" + "    470973.96\n"
        + "  ]\n" + "}";
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data =
        Map.of(FIELD_TEST, Map.of(geometryOperator, Map.of(SpatialConstants.FROM_GEOJSON, geoJson)));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(GeometryFilterCriteria.class));
    assertThat(((GeometryFilterCriteria) result.get(0)).getFilterOperator()
        .name()
        .toLowerCase(), is(geometryOperator));
    assertThat(((GeometryFilterCriteria) result.get(0)).getGeometry()
        .toString(), is("POINT (194936.73 470973.96)"));
  }

  @ParameterizedTest
  @CsvSource({"within", "contains", "intersects"})
  void parse_returnsFilterCriterias_forGeoJsonNotGeoFilters(String geometryOperator) {
    var geoJson = "{\n" + "  \"type\": \"Point\",\n" + "  \"coordinates\": [\n" + "    194936.73,\n" + "    470973.96\n"
        + "  ]\n" + "}";
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST,
        Map.of(FilterConstants.NOT_FIELD, Map.of(geometryOperator, Map.of(SpatialConstants.FROM_GEOJSON, geoJson))));

    List<FilterCriteria> result = parser.parse(typeConfiguration, inputObjectField, data);

    assertThat(result.size(), is(1));
    assertThat(result.get(0), instanceOf(NotFilterCriteria.class));

    var notFilterCriteria = (NotFilterCriteria) result.get(0);

    assertThat(((GeometryFilterCriteria) notFilterCriteria.getFilterCriteria()).getFilterOperator()
        .name()
        .toLowerCase(), is(geometryOperator));
    assertThat(((GeometryFilterCriteria) notFilterCriteria.getFilterCriteria()).getGeometry()
        .toString(), is("POINT (194936.73 470973.96)"));
  }

  @Test
  void parse_throwsException_forInvalidWkt() {
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(GeometryFilterOperator.CONTAINS.name()
        .toLowerCase(), Map.of(SpatialConstants.FROM_WKT, "1234")));

    var exception =
        assertThrows(IllegalArgumentException.class, () -> parser.parse(typeConfiguration, inputObjectField, data));

    assertThat(exception.getMessage(), is("The filter input WKT is invalid!"));
  }

  @Test
  void parse_throwsException_forInvalidWkb() {
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(GeometryFilterOperator.CONTAINS.name()
        .toLowerCase(), Map.of(SpatialConstants.FROM_WKB, "1234")));

    var exception =
        assertThrows(IllegalArgumentException.class, () -> parser.parse(typeConfiguration, inputObjectField, data));

    assertThat(exception.getMessage(), is("The filter input WKB is invalid!"));
  }

  @Test
  void parse_throwsException_forInvalidGeoJson() {
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(GeometryFilterOperator.CONTAINS.name()
        .toLowerCase(), Map.of(SpatialConstants.FROM_GEOJSON, "1234")));

    var exception =
        assertThrows(IllegalArgumentException.class, () -> parser.parse(typeConfiguration, inputObjectField, data));

    assertThat(exception.getMessage(), is("The filter input GeoJSON is invalid!"));
  }


  @Test
  void parse_throwsException_forInvalidFilterItemValueMap() {

    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of(GeometryFilterOperator.CONTAINS.name()
        .toLowerCase(), "1234"));

    var exception =
        assertThrows(IllegalArgumentException.class, () -> parser.parse(typeConfiguration, inputObjectField, data));

    assertThat(exception.getMessage(), is("Filter item value not of type Map!"));
  }

  @Test
  void parse_throwsException_forInvalidGeometryFilterMethod() {
    var wkt = "POINT (194936.73 470973.96)";
    var typeConfiguration = createTypeConfiguration(SpatialConstants.GEOMETRY);
    var inputObjectField = createInputObjectField(FIELD_TEST, SpatialConstants.GEOMETRY_FILTER);
    Map<String, Object> data = Map.of(FIELD_TEST, Map.of("contains", Map.of("vanGeometrieObject", wkt)));

    var exception =
        assertThrows(IllegalArgumentException.class, () -> parser.parse(typeConfiguration, inputObjectField, data));

    assertThat(exception.getMessage(), is(
        "The geometry filter does not contain one of the following methods: 'fromWKT', 'fromWKB' or, 'fromGeoJSON'."));
  }
}
