package org.dotwebstack.framework.service.openapi.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.AstPrinter;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.InternalServerErrorException;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

class GeometryTypeMapperTest {

  private static final GeometryTypeMapper typeMapper = new GeometryTypeMapper();

  @Test
  void schemaToField_selectsAsGeoJson_forObjectSchema() {
    var schema = new ObjectSchema();
    schemaToField_selectsAsGeoJson_forSchema(schema);
  }

  @Test
  void schemaToField_selectsAsGeoJson_forArraySchema() {
    var schema = new ArraySchema();
    schemaToField_selectsAsGeoJson_forSchema(schema);
  }

  @Test
  void schemaToField_selectsAsGeoJson_forSchemaWithObjectType() {
    var schema = new Schema<>().type("object");
    schemaToField_selectsAsGeoJson_forSchema(schema);
  }

  @Test
  void schemaToField_selectsAsGeoJson_forSchemaWithArrayType() {
    var schema = new Schema<>().type("array");
    schemaToField_selectsAsGeoJson_forSchema(schema);
  }

  private void schemaToField_selectsAsGeoJson_forSchema(Schema<?> schema) {
    var name = "location";

    var fields = typeMapper.schemaToField(name, schema);
    assertThat(fields.size(), is(1));

    var field = fields.get(0);
    assertThat(AstPrinter.printAstCompact(field), is("location {asGeoJSON}"));
  }

  @Test
  void schemaToField_throwsException_forInvalidSchema() {
    var schema = new StringSchema();

    Exception exception =
        assertThrows(InvalidConfigurationException.class, () -> typeMapper.schemaToField("location", schema));

    assertThat(exception.getMessage(), is("Geometry type requires an object or array schema type (found: string)."));
  }

  @Test
  void fieldToBody_returnsGeoJsonMap_forValidMapResultAndObject() {
    var data = Map.of("asGeoJSON", "{\"type\": \"Point\", \"coordinates\": [5.38720071, 52.15516755]}");

    var body = typeMapper.fieldToBody(data, new Schema<>().type("object"));

    assertThat(body, is(equalTo(Map.of("type", "Point", "coordinates", List.of(5.38720071, 52.15516755)))));
  }

  @Test
  void fieldToBody_returnsGeoJsonMap_forValidMapResultAndArray() {
    var data = Map.of("asGeoJSON", "{\"type\": \"GeometryCollection\", \"geometries\": "
        + "[{\"type\": \"Point\", \"coordinates\": [5.38720071, 52.15516755]}]}");

    var body = typeMapper.fieldToBody(data, new Schema<>().type("array"));

    assertThat(body, is(equalTo(List.of(Map.of("type", "Point", "coordinates", List.of(5.38720071, 52.15516755))))));
  }

  @Test
  void fieldToBody_returnsGeoJsonMap_forInvalidMapResult() {
    var data = Map.of("asGeoJSON", "{foo}");
    var schema = new Schema<>().type("object");

    Exception exception = assertThrows(InternalServerErrorException.class, () -> typeMapper.fieldToBody(data, schema));

    assertThat(exception.getMessage(), is("Error while parsing GeoJSON string."));
  }

  @Test
  void fieldToBody_throwsException_forNonMapResult() {
    var schema = new Schema<>().type("object");

    Exception exception =
        assertThrows(InvalidConfigurationException.class, () -> typeMapper.fieldToBody("foo", schema));

    assertThat(exception.getMessage(), is("Geometry type expects a map result."));
  }

  @Test
  void fieldToBody_throwsException_forMissingGeoJson() {
    var data = Map.of();
    var schema = new Schema<>().type("object");

    Exception exception = assertThrows(InvalidConfigurationException.class, () -> typeMapper.fieldToBody(data, schema));

    assertThat(exception.getMessage(), is("No key named `asGeoJSON` found in map result."));
  }

  @Test
  void fieldToBody_throwsException_forInvalidSchema() {
    var data = Map.of("asGeoJSON", "{\"type\": \"GeometryCollection\", \"geometries\": "
        + "[{\"type\": \"Point\", \"coordinates\": [5.38720071, 52.15516755]}]}");
    var schema = new StringSchema();

    Exception exception = assertThrows(InvalidConfigurationException.class, () -> typeMapper.fieldToBody(data, schema));

    assertThat(exception.getMessage(), is("Geometry type requires an object or array schema type (found: string)."));
  }

  @Test
  void fieldToBody_throwsException_forNonGeoJsonCollectionAndArray() {
    var data = Map.of("asGeoJSON", "{\"type\": \"Point\", \"coordinates\": [5.38720071, 52.15516755]}");
    var schema = new Schema<>().type("array");

    Exception exception = assertThrows(InvalidConfigurationException.class, () -> typeMapper.fieldToBody(data, schema));

    assertThat(exception.getMessage(), is("No key named 'geometries' found in map result."));
  }

  @Test
  void typeName_returnsGeometry_always() {
    assertThat(typeMapper.typeName(), is("Geometry"));
  }
}
