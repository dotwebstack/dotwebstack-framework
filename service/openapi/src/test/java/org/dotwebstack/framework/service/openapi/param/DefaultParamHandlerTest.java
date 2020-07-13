package org.dotwebstack.framework.service.openapi.param;

import static java.util.Arrays.asList;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.PARAM_HEADER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.PARAM_PATH_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.PARAM_QUERY_TYPE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DefaultParamHandlerTest {

  private static final String TYPE_ARRAY = "array";

  private static final String TYPE_OBJECT = "object";

  private static final String TYPE_STRING = "string";

  private static final String TYPE_INTEGER = "integer";

  private static final String TYPE_NUMBER = "number";

  private static final String FORMAT_DATE = "date";

  private static final String FORMAT_DATETIME = "date-time";

  @Mock
  private ServerRequest request;

  @Mock
  private Parameter parameter;

  @Mock
  private ResponseSchemaContext responseSchemaContext;

  @Mock
  private StringSchema schema;

  private DefaultParamHandler paramHandler;

  @BeforeEach
  public void setup() {
    this.paramHandler = new DefaultParamHandler(TestResources.openApi());
  }

  @Test
  public void getValue_returnsValue_fromPathParam() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals("v1", result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParam() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals("v1", result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryHeader() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals("v1", result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArraySimple() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArrayForm() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.FORM);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArraySpaceDelim() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1 v2 v3", TYPE_ARRAY, false, Parameter.StyleEnum.SPACEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArrayPipeDelim() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryhParamArraySimple() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamArrayForm() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.FORM);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamArraySpaceDelim() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1 v2 v3", TYPE_ARRAY, false, Parameter.StyleEnum.SPACEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamArrayPipeDelim() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArraySimple() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArrayForm() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.FORM);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArraySpaceDelim() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1 v2 v3", TYPE_ARRAY, false, Parameter.StyleEnum.SPACEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArrayPipeDelim() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_forArrayEnumCheck() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);
    mockArrayEnum(asList("v1", "v2", "v3", "v4"));

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_throwsException_forArrayEnumCheck() {
    // Arrange
    mockParameterHeader("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);
    mockArrayEnum(asList("v1", "v2"));

    // Act / Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_throwsException_forTypeValidationDateCheck() {
    // Arrange
    mockParameterHeader("test_date", "2019-03-99", TYPE_STRING, FORMAT_DATE, false, Parameter.StyleEnum.SIMPLE);

    // Act / Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_throwsException_forTypeValidationDateTimeCheck() {
    // Arrange
    mockParameterHeader("test_date-time", "2016-03-99T00:00:00+01:00", TYPE_STRING, FORMAT_DATETIME, false,
        Parameter.StyleEnum.SIMPLE);

    // Act / Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_returnsValue_forTypeInteger() {
    // Arrange
    mockParameterHeader("test_integer", "42", TYPE_INTEGER, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals("42", result.get());
  }

  @Test
  public void getValue_throwsException_forTypeValidationInteger_double() {
    // Arrange
    mockParameterHeader("test_integer", "4.2", TYPE_INTEGER, false, Parameter.StyleEnum.SIMPLE);

    // Act / Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_throwsException_forTypeValidationInteger_string() {
    // Arrange
    mockParameterQuery("test_integer", "string", TYPE_INTEGER, false, Parameter.StyleEnum.SIMPLE);

    // Act / Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_returnsValue_forTypeNumber_long() {
    // Arrange
    mockParameterHeader("test_number", "2147483648", TYPE_NUMBER, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals("2147483648", result.get());
  }

  @Test
  public void getValue_returnsValue_forTypeNumber_double() {
    // Arrange
    mockParameterHeader("test_number", "4.2", TYPE_NUMBER, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals("4.2", result.get());
  }

  @Test
  public void getValue_throwsException_forTypeValidationNumber_string() {
    // Arrange
    mockParameterPath("test_number", "string", TYPE_NUMBER, false, Parameter.StyleEnum.SIMPLE);

    // Act / Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_throwsException_withArraytUnsupportedStyle() {
    // Arrange
    mockParameterQuery("test", "v1,v2", TYPE_ARRAY, true, Parameter.StyleEnum.LABEL);

    // Act / Assert
    assertThrows(UnsupportedOperationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_returnsValue_fromQueryParamObjectSimple() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "k1,v1,k2,v2", TYPE_OBJECT, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamObjectSimpleExplode() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "k1=v1,k2=v2", TYPE_OBJECT, true, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), result.get());
  }

  @Test
  public void getValue_returnsDefaultValue_fromQueryParamString() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);
    when(request.pathVariable("test")).thenThrow(IllegalArgumentException.class);
    when(parameter.getSchema()
        .getDefault()).thenReturn("default1");

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter, responseSchemaContext);

    // Assert
    assertEquals("default1", result.get());
  }

  @Test
  public void getValue_throwsError_whenDefaultValueIsNotSet() {
    // Arrange
    mockParameterPath("test", "v", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);
    when(request.pathVariable("test")).thenThrow(IllegalArgumentException.class);
    when(parameter.getRequired()).thenReturn(true);

    // Act & Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_throwsError_whenDefaultDoesNotMatchEnum() {
    // Arrange
    mockParameterPath("test", "v", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);
    when(request.pathVariable("test")).thenThrow(IllegalArgumentException.class);
    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getDefault()).thenReturn("default1");
    when(schema.getType()).thenReturn(TYPE_STRING);
    when(schema.getEnum()).thenReturn(ImmutableList.of("default2"));

    // Act & Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_throwsException_withObjectUnsupportedStyle() {
    // Arrange
    mockParameterPath("test", "k1=v1,k2=v2", TYPE_OBJECT, true, Parameter.StyleEnum.FORM);

    // Act / Assert
    assertThrows(UnsupportedOperationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_throwsError_withNonMatchingPattern() {
    // Arrange
    mockParameterPath("test", "v", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);
    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getPattern()).thenReturn("[A-Z]+");
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_returns_withMatchingPattern() {
    // Arrange
    mockParameterPath("test", "VBA", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);
    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getPattern()).thenReturn("[A-Z]+");
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertDoesNotThrow(() -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getValue_returns_withNullPattern() {
    // Arrange
    mockParameterPath("test", "VBA", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);
    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getPattern()).thenReturn(null);
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertDoesNotThrow(() -> paramHandler.getValue(request, parameter, responseSchemaContext));
  }

  @Test
  public void getDefault_returnsListForArrayType() {
    // Arrange
    PathItem query3 = TestResources.openApi()
        .getPaths()
        .get("/query3/{query3_param1}");

    // Act
    Optional<Object> defaultValue = paramHandler.getDefault(query3.getGet()
        .getParameters()
        .get(0));

    // Assert
    assertEquals(asList("default1", "default2"), defaultValue.orElseGet(() -> new NullPointerException("")));
  }

  @Test
  public void getDefault_returnsStringForStringType() {
    // Arrange
    PathItem query3 = TestResources.openApi()
        .getPaths()
        .get("/query3/{query3_param1}");

    // Act
    Optional<Object> defaultValue = paramHandler.getDefault(query3.getGet()
        .getParameters()
        .get(1));

    // Assert
    assertEquals("query3_param2_default", defaultValue.orElseGet(() -> new NullPointerException("")));
  }

  @Test
  public void getDefault_returnsMapForInlineObjectType() {
    // Arrange
    PathItem query3 = TestResources.openApi()
        .getPaths()
        .get("/query3/{query3_param1}");

    // Act
    Optional<Object> defaultValue = paramHandler.getDefault(query3.getGet()
        .getParameters()
        .get(2));

    // Assert
    assertEquals(ImmutableMap.of("p1", "v1", "p2", "v2"), defaultValue.orElseGet(() -> new NullPointerException("")));
  }

  @Test
  public void getDefault_returnsMapForRefObjectType() {
    // Arrange
    PathItem query3 = TestResources.openApi()
        .getPaths()
        .get("/query3/{query3_param1}");

    // Act
    Optional<Object> defaultValue = paramHandler.getDefault(query3.getGet()
        .getParameters()
        .get(3));

    // Assert
    assertEquals(ImmutableMap.of("o2_prop1", "v1"), defaultValue.orElseGet(() -> new NullPointerException("")));
  }


  private void mockParameterPath(String name, String value, String type, boolean explode, Parameter.StyleEnum style) {
    when(parameter.getIn()).thenReturn(PARAM_PATH_TYPE);
    when(request.pathVariable(name)).thenReturn(value);
    mockParameter(name, type, null, explode, style);
  }

  private void mockParameterQuery(String name, String value, String type, boolean explode, Parameter.StyleEnum style) {
    when(parameter.getIn()).thenReturn(PARAM_QUERY_TYPE);
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add(name, value);
    when(request.queryParams()).thenReturn(map);
    mockParameter(name, type, null, explode, style);
  }

  private void mockParameterHeader(String name, String value, String type, boolean explode, Parameter.StyleEnum style) {
    mockParameterHeader(name, value, type, null, explode, style);
  }

  private void mockParameterHeader(String name, String value, String type, String format, boolean explode,
      Parameter.StyleEnum style) {
    when(parameter.getIn()).thenReturn(PARAM_HEADER_TYPE);
    ServerRequest.Headers headers = mock(ServerRequest.Headers.class);
    when(headers.header(name)).thenReturn(asList(value));
    when(request.headers()).thenReturn(headers);
    mockParameter(name, type, format, explode, style);
  }

  @SuppressWarnings("rawtypes")
  private void mockArrayEnum(List<String> value) {
    Schema itemSchema = ((ArraySchema) parameter.getSchema()).getItems();
    when(itemSchema.getEnum()).thenReturn(value);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void mockParameter(String name, String type, String format, boolean explode, Parameter.StyleEnum style) {
    when(parameter.getName()).thenReturn(name);
    when(parameter.getExplode()).thenReturn(explode);
    when(parameter.getStyle()).thenReturn(style);
    Schema schema;
    switch (type) {
      case ARRAY_TYPE:
        ArraySchema arraySchema = mock(ArraySchema.class);
        Schema itemSchema = mock(StringSchema.class);
        when(itemSchema.getEnum()).thenReturn(null);
        when(arraySchema.getItems()).thenReturn(itemSchema);

        schema = arraySchema;
        break;
      case OBJECT_TYPE:
        schema = mock(ObjectSchema.class);
        break;
      default:
        schema = mock(Schema.class);
    }
    when(schema.getEnum()).thenReturn(null);

    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getType()).thenReturn(type);
    when(schema.getFormat()).thenReturn(format);
  }
}
