package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createFieldContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.OpenApiProperties;
import org.dotwebstack.framework.service.openapi.conversion.LocalDateTypeConverter;
import org.dotwebstack.framework.service.openapi.conversion.TypeConverterRouter;
import org.dotwebstack.framework.service.openapi.conversion.ZonedDateTimeTypeConverter;
import org.dotwebstack.framework.service.openapi.exception.NotFoundException;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.response.FieldContext;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@ExtendWith(MockitoExtension.class)
class JsonResponseMapperTest {
  private static final ResponseObject REQUIRED_NILLABLE_STRING = getProperty("prop1", "string", true, true, null);

  private static final ResponseObject REQUIRED_NON_NILLABLE_STRING = getProperty("prop2", "string", true, false, null);

  private static final ResponseObject DWS_TEMPLATE = getProperty("prop4", "string", true, false,
      "`${env.env_var_1}_${fields.prop2}_${fields._parent.prop2}_${fields._parent._parent.prop2}_${args._parent"
          + "._parent.arg1}_${data}`");

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  @Mock
  private EnvironmentProperties properties;

  private JsonResponseMapper jsonResponseMapper;

  @Mock
  private OpenApiProperties openApiProperties;

  @Mock
  private OpenApiProperties.DateFormatProperties dateFormatProperties;

  private TypeConverterRouter typeConverterRouter;

  @Mock
  private GraphQlField graphQlField;

  @Mock
  private Schema<String> mockSchema;

  @BeforeEach
  void setup() {
    when(openApiProperties.getDateproperties()).thenReturn(dateFormatProperties);

    this.typeConverterRouter = new TypeConverterRouter(
        List.of(new ZonedDateTimeTypeConverter(openApiProperties), new LocalDateTypeConverter(openApiProperties)));
    this.jsonResponseMapper =
        new JsonResponseMapper(new Jackson2ObjectMapperBuilder(), jexlEngine, properties, typeConverterRouter);
  }

  @Test
  void map_returnsProperty_ForValidResponse() throws NotFoundException {
    // Arrange
    Object data = ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "prop1value");
    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(data, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(getObject("root", ImmutableList.of(REQUIRED_NILLABLE_STRING)))
        .data(ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "prop1value"))
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.equals("{\"prop1\":\"prop1value\"}"));
  }

  @Test
  void map_returnsException_ForMissingRequiredProperty() {
    // Arrange
    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .responseObject(getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING)))
        .build();

    // Act & Assert
    assertThrows(NotFoundException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }

  @Test
  void map_throwsException_ForMissingRequiredNonNillableProperty() {
    // Arrange
    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING)))
        .data(ImmutableMap.of("other key", "prop1value"))
        .build();

    // Act / Assert
    assertThrows(NotFoundException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }

  @Test
  void map_returnsValue_forDwsTemplate() throws NotFoundException {
    // Arrange
    when(properties.getAllProperties()).thenReturn(ImmutableMap.of("env_var_1", "v0"));
    ResponseObject child2 = getObject("child2", ImmutableList.of(DWS_TEMPLATE));
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child2));
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child1));

    Map<String, Object> child2Data =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3", DWS_TEMPLATE.getIdentifier(), "dummy");
    Map<String, Object> child1Data =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2", "child2", child2Data);
    Map<String, Object> rootData =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v1", "child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, ImmutableMap.of("arg1", "arg_v1")));

    URI uri = URI.create("http://dontcare.com:90210/bh?a=b");

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .parameters(Collections.emptyMap())
        .uri(uri)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    String expectedSubString = "\"prop4\":\"v0_v3_v2_v1_arg_v1_dummy\"";
    assertTrue(response.contains(expectedSubString), String
        .format("Expected sub string [%s] not found in " + "returned response [%s]", expectedSubString, response));
  }

  @Test
  void map_returnsValue_forResponseWithEnvelopeObjectValue() throws NotFoundException {
    // Arrange
    ResponseObject child2 = getObject("child2", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject embedded = getObject("_embedded", "object", true, null, ImmutableList.of(child2), new ArrayList<>());
    ResponseObject child1 = getObject("child1", ImmutableList.of(embedded));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data = ImmutableMap.of("child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"_embedded\":{\"child2\":{\"prop2\":\"v3\"}}}}"));
  }

  @Test
  void map_returnsValue_forResponseWithEmbeddedEnvelopeObjectValue() throws NotFoundException {
    // Arrange
    ResponseObject child2 = getObject("child2", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject embedded1 =
        getObject("_embedded", "object", true, null, ImmutableList.of(child2), new ArrayList<>());
    ResponseObject embedded2 =
        getObject("_embedded", "object", true, null, ImmutableList.of(embedded1), new ArrayList<>());
    ResponseObject child1 = getObject("child1", ImmutableList.of(embedded2));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data = ImmutableMap.of("child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"_embedded\":{\"_embedded\":{\"child2\":{\"prop2\":\"v3\"}}}}}"));
  }

  @Test
  void map_returnsValue_forResponseWithComposedSchema() throws NotFoundException {
    // Arrange
    ResponseObject child1 = getObject("response", "object", false, null, ImmutableList.of(REQUIRED_NILLABLE_STRING),
        Collections.emptyList());
    ResponseObject child2 = getObject("response", "object", false, null, ImmutableList.of(REQUIRED_NON_NILLABLE_STRING),
        Collections.emptyList());
    ResponseObject responseObject =
        getObject("response", "object", false, null, null, ImmutableList.of(child1, child2));

    Map<String, Object> rootData = ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "v3",
        REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2");

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("\"prop1\":\"v3\""));
    assertTrue(response.contains("\"prop2\":\"v2\""));
  }

  @Test
  void map_returnsValue_forResponseWithComposedEnvelopeSchema() throws NotFoundException {
    // Arrange
    ResponseObject child1 = getObject("response", "object", false, null, ImmutableList.of(REQUIRED_NILLABLE_STRING),
        Collections.emptyList());
    ResponseObject child2 = getObject("response", "object", false, null, ImmutableList.of(REQUIRED_NON_NILLABLE_STRING),
        Collections.emptyList());
    ResponseObject responseObject =
        getObject("response", "object", true, null, Collections.emptyList(), ImmutableList.of(child1, child2));

    Map<String, Object> rootData = ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "v3",
        REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2");

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("\"prop1\":\"v3\""));
    assertTrue(response.contains("\"prop2\":\"v2\""));
  }

  @Test
  void map_returnsValue_forResponseWithArray() throws NotFoundException {
    // Arrange
    ResponseObject arrayObject1 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject arrayObject2 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject array1 =
        getObject("array1", "array", false, ImmutableList.of(arrayObject1, arrayObject2), null, new ArrayList<>());
    ResponseObject child1 = getObject("child1", ImmutableList.of(array1));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> arrayObject1Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> arrayObject2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    List<Object> array1Data = ImmutableList.of(arrayObject1Data, arrayObject2Data);
    Map<String, Object> child1Data = ImmutableMap.of("array1", array1Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();


    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"array1\":[{\"prop2\":\"v3\"},{\"prop2\":\"v3\"}]}}"));
  }

  @Test
  void map_returnsValue_forResponseWithDefaultArrayForEnvelope() throws NotFoundException {
    // Arrange
    ResponseObject arrayObject1 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject arrayObject2 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject array1 = getObject("array1", "array", true, false, true, null,
        ImmutableList.of(arrayObject1, arrayObject2), new ArrayList<>(), getArraySchema(List.of("defaultvalue")));
    ResponseObject child1 = getObject("child1", ImmutableList.of(array1));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> arrayObject1Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> arrayObject2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    List<Object> array1Data = ImmutableList.of(arrayObject1Data, arrayObject2Data);
    Map<String, Object> child1Data = ImmutableMap.of("array1", array1Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();


    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"array1\":[\"defaultvalue\"]}}"));
  }

  @Test
  void map_returnsValue_forResponseWithDefaultScalarForEnvelope() throws NotFoundException {
    // Arrange
    StringSchema stringSchema = new StringSchema();
    stringSchema.extensions(Map.of(OasConstants.X_DWS_DEFAULT, "defaultValue"));

    ResponseObject prop1 = getProperty("prop1", "string", true, true, null, true, stringSchema);
    ResponseObject child1 = getObject("child1", ImmutableList.of(prop1));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = ImmutableMap.of("prop2", "");
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();


    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"prop1\":\"defaultValue\"}}"));
  }

  @Test
  void map_returnsValue_forResponseWithDefaultScalarForEnvelopeTypeMismatch() throws NotFoundException {
    // Arrange
    StringSchema stringSchema = new StringSchema();
    stringSchema.extensions(Map.of(OasConstants.X_DWS_DEFAULT, 1));

    ResponseObject prop1 = getProperty("prop1", "string", true, true, null, true, stringSchema);
    ResponseObject child1 = getObject("child1", ImmutableList.of(prop1));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = ImmutableMap.of("prop2", "");
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act && Assert
    assertThrows(MappingException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }

  @Test
  void map_returnsValue_forResponseWithDefaultArray() throws NotFoundException {
    // Arrange
    ResponseObject arrayObject1 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject arrayObject2 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject array1 = getObject("array1", "array", true, false, false, null,
        ImmutableList.of(arrayObject1, arrayObject2), new ArrayList<>(), getArraySchema(List.of("defaultvalue")));
    ResponseObject child1 = getObject("child1", ImmutableList.of(array1));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();


    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"array1\":[\"defaultvalue\"]}}"));
  }

  @Test
  void map_returnsException_forResponseWithDefaultArrayWithInvalidType() throws NotFoundException {
    // Arrange
    ResponseObject arrayObject1 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject arrayObject2 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject array1 = getObject("array1", "array", true, false, false, null,
        ImmutableList.of(arrayObject1, arrayObject2), new ArrayList<>(), getArraySchema("defaultvalue"));
    ResponseObject child1 = getObject("child1", ImmutableList.of(array1));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();


    // Act & Assert
    assertThrows(MappingException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }

  @Test
  void toResponse_returnsNoElement_forNonRequiredNonNillableEmptyArray() throws NotFoundException {
    // Arrange
    ResponseObject array = getObject("array1", "array", false, false, false, null, null, new ArrayList<>());
    ResponseObject child1 = getObject("child1", ImmutableList.of(array));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":null}"));
  }

  @Test
  void toResponse_returnsNoElement_forNonRequiredNullableNullArray() throws NotFoundException {
    // Arrange
    ResponseObject array = getObject("array1", "array", false, true, false, null, null, new ArrayList<>());
    ResponseObject child1 = getObject("child1", ImmutableList.of(array));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":null}"));
  }

  @Test
  void toResponse_returnsEmptyList_forRequiredNonNullableNullArray() throws NotFoundException {
    // Arrange
    ResponseObject array = getObject("array1", "array", true, false, false, null, null, new ArrayList<>());
    ResponseObject child1 = getObject("child1", ImmutableList.of(array));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"array1\":[]}}"));
  }

  @Test
  void toResponse_returnsNull_forRequiredNullableNullArray() throws NotFoundException {
    // Arrange
    ResponseObject array = getObject("array1", "array", true, true, false, null, null, new ArrayList<>());
    ResponseObject child1 = getObject("child1", ImmutableList.of(array));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"array1\":null}}"));
  }

  @Test
  void toResponse_returnsDefaultValue_forInvalidScriptAndNullFallback() {
    // Arrange
    when(mockSchema.getDefault()).thenReturn("default");

    Object data = ImmutableMap.of("prop1", "prop1value");
    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(data, Collections.emptyMap()));

    Map<String, String> map = new HashMap<>();
    map.put("value", "args.field1");
    map.put("fallback", null);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .uri(URI.create("http://dontcare.com:90210/bh?a=b"))
        .graphQlField(graphQlField)
        .responseObject(ResponseObject.builder()
            .identifier("prop1")
            .summary(SchemaSummary.builder()
                .type("string")
                .required(true)
                .nillable(false)
                .dwsExpr(map)
                .schema(mockSchema)
                .build())
            .build())
        .data(ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "prop1value"))
        .dataStack(dataStack)
        .build();

    // Act
    Object actual = jsonResponseMapper.mapScalarDataToResponse(writeContext);

    // Assert
    assertEquals("default", actual);
  }

  @Test
  void map_returnsValue_forResponseWithObject() throws NotFoundException {
    // Arrange
    ResponseObject child2 = getObject("child2", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child2));
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2", "child2", child2Data);
    Map<String, Object> rootData =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v1", "child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"prop2\":\"v1\",\"child1\":{\"prop2\":\"v2\",\"child2\":{\"prop2\":\"v3\"}}}"));
  }

  @Test
  void map_returnsValue_withoutIncludedObject() {
    // Arrange
    ResponseWriteContext writeContext = arrangeIncludeWriteContext("prop2 != `v3`");

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertEquals(response, "{\"prop2\":\"v1\",\"child1\":{\"prop2\":\"v2\"}}");
  }

  @Test
  void map_returnsValue_withIncludedObject() {
    // Arrange
    ResponseWriteContext writeContext = arrangeIncludeWriteContext("prop2 == `v3`");

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertEquals(response, "{\"prop2\":\"v1\",\"child1\":{\"prop2\":\"v2\",\"child2\":{\"prop2\":\"v3\"}}}");
  }

  private ResponseWriteContext arrangeIncludeWriteContext(String condition) {
    ObjectSchema objectSchema = new ObjectSchema();
    objectSchema.addExtension(OasConstants.X_DWS_INCLUDE, condition);

    ResponseObject child2 = getObject("child2", "object", false, false, false, null,
        ImmutableList.of(REQUIRED_NON_NILLABLE_STRING), new ArrayList<>(), objectSchema);
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child2));

    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2", "child2", child2Data);
    Map<String, Object> rootData =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v1", "child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    return ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();
  }

  @Test
  void map_returnsNullObject_forEmptyObjectWithIdentifyingField() throws NotFoundException {
    // Arrange
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = Collections.emptyMap();
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":null}"));
  }

  @Test
  void map_returnsObjectWithNullFields_forEmptyObjectWithoutIdentifyingField() throws NotFoundException {
    // Arrange
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NILLABLE_STRING));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = Collections.emptyMap();
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = jsonResponseMapper.toResponse(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"prop1\":null}}"));
  }

  @Test
  void map_throwsException_forMissingNonNillableRequiredField() throws NotFoundException {
    // Arrange
    ResponseObject child1 =
        getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, REQUIRED_NILLABLE_STRING));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child1Data = Map.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "v1");
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .graphQlField(graphQlField)
        .responseObject(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act / Assert
    assertThrows(MappingException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }

  @Test
  void validate_removeRoot_withDoubleValuePath() {
    // Arrange
    String testString = "test.test";

    // Act
    String resultString = jsonResponseMapper.removeRoot(testString);

    // Assert
    assertEquals("test", resultString);
  }

  @Test
  void validate_removeRoot_withMultivaluePath() {
    // Arrange
    String testString = "test.test.test";

    // Act
    String resultString = jsonResponseMapper.removeRoot(testString);

    // Assert
    assertEquals("test.test", resultString);
  }

  @Test
  void validate_removeRoot_withSinleValuePath() {
    // Arrange
    String testString = "test";

    // Act
    String resultString = jsonResponseMapper.removeRoot(testString);

    // Assert
    assertEquals("", resultString);
  }

  private static ArraySchema getArraySchema(Object defaultValue) {
    ArraySchema arraySchema = new ArraySchema().type("string");
    arraySchema.extensions(Map.of(OasConstants.X_DWS_DEFAULT, defaultValue));
    return arraySchema;
  }

  private static ResponseObject getObject(String identifier, List<ResponseObject> children) {
    return getObject(identifier, "object", true, false, null, children, new ArrayList<>());
  }

  private static ResponseObject getObject(String identifier, String type, boolean required, boolean nullable,
      boolean envelop, List<ResponseObject> items, List<ResponseObject> children, List<ResponseObject> composedOf) {
    return getObject(identifier, type, required, nullable, envelop, items, children, composedOf, null);
  }

  private static ResponseObject getObject(String identifier, String type, boolean required, boolean nullable,
      boolean envelop, List<ResponseObject> items, List<ResponseObject> children, List<ResponseObject> composedOf,
      Schema<?> schema) {
    return ResponseObject.builder()
        .identifier(identifier)
        .summary(SchemaSummary.builder()
            .type(type)
            .required(required)
            .nillable(nullable)
            .children(children)
            .items(items)
            .composedOf(composedOf)
            .isTransient(envelop)
            .schema(schema)
            .build())
        .build();
  }

  private static ResponseObject getObject(String identifier, String type, boolean envelop, List<ResponseObject> items,
      List<ResponseObject> children, List<ResponseObject> composedOf) {
    return getObject(identifier, type, true, envelop, items, children, composedOf);
  }

  private static ResponseObject getObject(String identifier, String type, boolean required, boolean envelop,
      List<ResponseObject> items, List<ResponseObject> children, List<ResponseObject> composedOf) {
    return getObject(identifier, type, required, true, envelop, items, children, composedOf);
  }

  private static ResponseObject getProperty(String identifier, String type, boolean required, boolean nillable,
      String dwsTemplate) {
    return getProperty(identifier, type, required, nillable, dwsTemplate, false, null);
  }

  private static ResponseObject getProperty(String identifier, String type, boolean required, boolean nillable,
      String dwsTemplate, boolean envelope, Schema<?> schema) {
    return ResponseObject.builder()
        .identifier(identifier)
        .summary(SchemaSummary.builder()
            .type(type)
            .required(true)
            .isTransient(envelope)
            .required(required)
            .nillable(nillable)
            .dwsExpr(Objects.nonNull(dwsTemplate) ? Map.of("value", dwsTemplate) : null)
            .schema(schema)
            .build())
        .build();
  }
}
