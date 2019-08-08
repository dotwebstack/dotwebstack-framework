package org.dotwebstack.framework.service.openapi.param;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.Optional;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.junit.jupiter.api.Assertions;
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

  @Mock
  private ServerRequest request;

  @Mock
  private Parameter parameter;

  private DefaultParamHandler paramHandler;

  @BeforeEach
  public void setup() {
    this.paramHandler = new DefaultParamHandler();
  }

  @Test
  public void getValue_returnsValue_fromPathParam() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals("v1", result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParam() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals("v1", result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryHeader() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals("v1", result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArraySimple() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArrayForm() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.FORM);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArraySpaceDelim() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1 v2 v3", TYPE_ARRAY, false, Parameter.StyleEnum.SPACEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromPathParamArrayPipeDelim() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryhParamArraySimple() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamArrayForm() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.FORM);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamArraySpaceDelim() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1 v2 v3", TYPE_ARRAY, false, Parameter.StyleEnum.SPACEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamArrayPipeDelim() throws ParameterValidationException {
    // Arrange
    mockParameterQuery("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArraySimple() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArrayForm() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1,v2,v3", TYPE_ARRAY, false, Parameter.StyleEnum.FORM);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArraySpaceDelim() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1 v2 v3", TYPE_ARRAY, false, Parameter.StyleEnum.SPACEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromHeaderParamArrayPipeDelim() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_returnsValue_forArrayEnumCheck() throws ParameterValidationException {
    // Arrange
    mockParameterHeader("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);
    mockArrayEnum(asList("v1", "v2", "v3", "v4"));

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableList.of("v1", "v2", "v3"), result.get());
  }

  @Test
  public void getValue_throwsException_forArrayEnumCheck() {
    // Arrange
    mockParameterHeader("test", "v1|v2|v3", TYPE_ARRAY, false, Parameter.StyleEnum.PIPEDELIMITED);
    mockArrayEnum(asList("v1", "v2"));

    // Act / Assert
    Assertions.assertThrows(ParameterValidationException.class, () -> paramHandler.getValue(request, parameter));
  }


  @Test
  public void getValue_throwsException_withArraytUnsupportedStyle() {
    // Arrange
    mockParameterQuery("test", "v1,v2", TYPE_ARRAY, true, Parameter.StyleEnum.LABEL);

    // Act / Assert
    Assertions.assertThrows(UnsupportedOperationException.class, () -> paramHandler.getValue(request, parameter));
  }

  @Test
  public void getValue_returnsValue_fromQueryParamObjectSimple() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "k1,v1,k2,v2", TYPE_OBJECT, false, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), result.get());
  }

  @Test
  public void getValue_returnsValue_fromQueryParamObjectSimpleExplode() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "k1=v1,k2=v2", TYPE_OBJECT, true, Parameter.StyleEnum.SIMPLE);

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), result.get());
  }

  @Test
  public void getValue_returnsDefaultValue_fromQueryParaString() throws ParameterValidationException {
    // Arrange
    mockParameterPath("test", "v", TYPE_STRING, false, Parameter.StyleEnum.SIMPLE);
    when(request.pathVariable("test")).thenThrow(IllegalArgumentException.class);
    when(parameter.getSchema()
        .getDefault()).thenReturn("default");

    // Act
    Optional<Object> result = paramHandler.getValue(request, parameter);

    // Assert
    assertEquals("default", result.get());
  }

  @Test
  public void getValue_throwsException_withObjectUnsupportedStyle() {
    // Arrange
    mockParameterPath("test", "k1=v1,k2=v2", TYPE_OBJECT, true, Parameter.StyleEnum.FORM);

    // Act / Assert
    Assertions.assertThrows(UnsupportedOperationException.class, () -> paramHandler.getValue(request, parameter));
  }


  private void mockParameterPath(String name, String value, String type, boolean explode, Parameter.StyleEnum style) {
    when(parameter.getIn()).thenReturn("path");
    when(request.pathVariable(name)).thenReturn(value);
    mockParameter(name, type, explode, style);
  }

  private void mockParameterQuery(String name, String value, String type, boolean explode, Parameter.StyleEnum style) {
    when(parameter.getIn()).thenReturn("query");
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add(name, value);
    when(request.queryParams()).thenReturn(map);
    mockParameter(name, type, explode, style);
  }

  private void mockParameterHeader(String name, String value, String type, boolean explode, Parameter.StyleEnum style) {
    when(parameter.getIn()).thenReturn("header");
    ServerRequest.Headers headers = mock(ServerRequest.Headers.class);
    when(headers.header(name)).thenReturn(asList(value));
    when(request.headers()).thenReturn(headers);
    mockParameter(name, type, explode, style);
  }

  @SuppressWarnings("rawtypes")
  private void mockArrayEnum(List<String> value) {
    Schema itemSchema = ((ArraySchema) parameter.getSchema()).getItems();
    when(itemSchema.getEnum()).thenReturn(value);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void mockParameter(String name, String type, boolean explode, Parameter.StyleEnum style) {
    when(parameter.getName()).thenReturn(name);
    when(parameter.getExplode()).thenReturn(explode);
    when(parameter.getStyle()).thenReturn(style);
    Schema schema;
    switch (type) {
      case "array":
        ArraySchema arraySchema = mock(ArraySchema.class);
        Schema itemSchema = mock(StringSchema.class);
        when(itemSchema.getEnum()).thenReturn(null);
        when(arraySchema.getItems()).thenReturn(itemSchema);

        schema = arraySchema;
        break;
      case "object":
        schema = mock(ObjectSchema.class);
        break;
      default:
        schema = mock(Schema.class);
    }
    when(schema.getEnum()).thenReturn(null);

    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getType()).thenReturn(type);
  }
}
