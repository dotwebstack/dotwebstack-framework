package org.dotwebstack.framework.service.openapi.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.AstPrinter;
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
    var name = "location";
    var schema = new ObjectSchema();

    var fields = typeMapper.schemaToField(name, schema);
    assertThat(fields.size(), is(1));

    var field = fields.get(0);
    assertThat(AstPrinter.printAstCompact(field), is("location {asGeoJSON}"));
  }

  @Test
  void schemaToField_selectsAsGeoJson_forSchemaWithObjectType() {
    var name = "location";
    var schema = new Schema<>().type("object");

    var fields = typeMapper.schemaToField(name, schema);
    assertThat(fields.size(), is(1));

    var field = fields.get(0);
    assertThat(AstPrinter.printAstCompact(field), is("location {asGeoJSON}"));
  }

  @Test
  void schemaToField_throwsException_forNonObjectSchema() {
    var schema = new StringSchema();

    assertThrows(InvalidConfigurationException.class, () -> typeMapper.schemaToField("location", schema));
  }

  @Test
  void fieldToBody_returnsGeoJsonMap_forValidMapResult() {
    var data = Map.of("asGeoJSON", "{\"type\": \"Point\", \"coordinates\": [5.38720071, 52.15516755]}");

    var body = typeMapper.fieldToBody(data);

    assertThat(body, is(equalTo(Map.of("type", "Point", "coordinates", List.of(5.38720071, 52.15516755)))));
  }

  @Test
  void fieldToBody_returnsGeoJsonMap_forInvalidMapResult() {
    var data = Map.of("asGeoJSON", "{foo}");

    assertThrows(InternalServerErrorException.class, () -> typeMapper.fieldToBody(data));
  }

  @Test
  void fieldToBody_throwsException_forNonMapResult() {
    assertThrows(InvalidConfigurationException.class, () -> typeMapper.fieldToBody("foo"));
  }

  @Test
  void typeName_returnsGeometry_always() {
    assertThat(typeMapper.typeName(), is("Geometry"));
  }
}
