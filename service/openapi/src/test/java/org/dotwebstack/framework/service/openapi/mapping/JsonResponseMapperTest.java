package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createFieldContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.Schema;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.OpenApiProperties;
import org.dotwebstack.framework.service.openapi.conversion.LocalDateTypeConverter;
import org.dotwebstack.framework.service.openapi.conversion.TypeConverterRouter;
import org.dotwebstack.framework.service.openapi.conversion.ZonedDateTimeTypeConverter;
import org.dotwebstack.framework.service.openapi.exception.NotFoundException;
import org.dotwebstack.framework.service.openapi.response.FieldContext;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.oas.OasArrayField;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;
import org.dotwebstack.framework.service.openapi.response.oas.OasObjectField;
import org.dotwebstack.framework.service.openapi.response.oas.OasScalarExpressionField;
import org.dotwebstack.framework.service.openapi.response.oas.OasScalarField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@ExtendWith(MockitoExtension.class)
class JsonResponseMapperTest {
  private static final Map<String, OasField> REQUIRED_NILLABLE_STRING =
      getScalarFieldMap("prop1", "string", true, true, null);

  private static final Map<String, OasField> REQUIRED_NON_NILLABLE_STRING =
      getScalarFieldMap("prop2", "string", true, false, null);

  private static final Map<String, OasField> DWS_TEMPLATE = getScalarFieldMap("prop4", "string", true, false,
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

  @Mock
  private Schema<String> mockSchema;

  @Mock
  private org.dotwebstack.framework.core.model.Schema schema;

  @BeforeEach
  void setup() {
    when(openApiProperties.getDateproperties()).thenReturn(dateFormatProperties);
    when(schema.usePaging()).thenReturn(true);

    TypeConverterRouter typeConverterRouter = new TypeConverterRouter(
        List.of(new ZonedDateTimeTypeConverter(openApiProperties), new LocalDateTypeConverter(openApiProperties)));
    this.jsonResponseMapper =
        new JsonResponseMapper(new Jackson2ObjectMapperBuilder(), jexlEngine, properties, typeConverterRouter, schema);
  }

  @Test
  void map_returnsProperty_ForValidResponse() throws NotFoundException {
    Object data = ImmutableMap.of(firstKey(REQUIRED_NILLABLE_STRING), "prop1value");
    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(data, Collections.emptyMap()));

    OasField field = getObjectField(REQUIRED_NILLABLE_STRING);
    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(field)
        .data(ImmutableMap.of(firstKey(REQUIRED_NILLABLE_STRING), "prop1value"))
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertEquals("{\"prop1\":\"prop1value\"}", response);
  }


  @Test
  void map_returnsException_ForMissingRequiredProperty() {
    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(getObjectField(REQUIRED_NILLABLE_STRING))
        .build();

    assertThrows(NotFoundException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }


  @Test
  void map_throwsException_ForMissingRequiredNonNillableProperty() {
    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(getObjectField(REQUIRED_NON_NILLABLE_STRING))
        .data(ImmutableMap.of("other key", "prop1value"))
        .build();

    assertThrows(NotFoundException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }

  @Test
  void map_returnsValue_forDwsTemplate() throws NotFoundException {
    when(properties.getAllProperties()).thenReturn(ImmutableMap.of("env_var_1", "v0"));
    OasField child2 = getObjectField(DWS_TEMPLATE);
    OasField child1 = getObjectField(merge(Map.of("child2", child2), REQUIRED_NON_NILLABLE_STRING));
    OasField rootField = getObjectField(merge(Map.of("child1", child1), REQUIRED_NON_NILLABLE_STRING));

    Map<String, Object> child2Data =
        ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3", firstKey(DWS_TEMPLATE), "dummy");
    Map<String, Object> child1Data =
        ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v2", "child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v1", "child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, ImmutableMap.of("arg1", "arg_v1")));

    URI uri = URI.create("http://dontcare.com:90210/bh?a=b");

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .oasField(rootField)
        .identifier("root")
        .data(rootData)
        .dataStack(dataStack)
        .parameters(Collections.emptyMap())
        .uri(uri)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    String expectedSubString = "\"prop4\":\"v0_v3_v2_v1_arg_v1_dummy\"";
    System.out.println(response);
    assertTrue(response.contains(expectedSubString), String
        .format("Expected sub string [%s] not found in " + "returned response [%s]", expectedSubString, response));
  }

  @Test
  void map_returnsValue_forResponseWithEnvelopeObjectValue() throws NotFoundException {
    OasField child2 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    OasField embedded = getEnvelopeObjectField(Map.of("child2", child2));
    OasField child1 = getObjectField(Map.of("_embedded", embedded));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child2Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    Map<String, Object> child1Data = ImmutableMap.of("child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"_embedded\":{\"child2\":{\"prop2\":\"v3\"}}}}"));
  }

  @Test
  void map_returnsValue_forResponseWithEmbeddedEnvelopeObjectValue() throws NotFoundException {
    OasField child2 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    OasField embedded1 = getEnvelopeObjectField(Map.of("child2", child2));
    OasField embedded2 = getEnvelopeObjectField(Map.of("_embedded", embedded1));
    OasField child1 = getObjectField(Map.of("_embedded", embedded2));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child2Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    Map<String, Object> child1Data = ImmutableMap.of("child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .oasField(rootField)
        .identifier("root")
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"_embedded\":{\"_embedded\":{\"child2\":{\"prop2\":\"v3\"}}}}}"));
  }


  @Test
  void map_returnsValue_forResponseWithArray() throws NotFoundException {
    OasField arrayObject = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    OasField array1 = getArrayField(arrayObject);
    OasField child1 = getObjectField(Map.of("array1", array1));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> arrayObject1Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    Map<String, Object> arrayObject2Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    List<Object> array1Data = ImmutableList.of(arrayObject1Data, arrayObject2Data);
    Map<String, Object> child1Data = ImmutableMap.of("array1", array1Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()

        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"array1\":[{\"prop2\":\"v3\"},{\"prop2\":\"v3\"}]}}"));
  }


  @Test
  void map_returnsValue_forResponseWithDefaultArrayForEnvelope() throws NotFoundException {
    OasField arrayObject1 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    OasField array1 = getArrayField(arrayObject1);
    array1.setDefaultValue(List.of("defaultValue"));
    OasField child1 = getObjectField(Map.of("array1", array1));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> arrayObject1Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    Map<String, Object> arrayObject2Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    List<Object> array1Data = ImmutableList.of(arrayObject1Data, arrayObject2Data);
    Map<String, Object> child1Data = ImmutableMap.of("array1", array1Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"array1\":[\"defaultValue\"]}}"));
  }

  @Test
  void map_returnsValue_forResponseWithDefaultScalarForEnvelope() throws NotFoundException {
    OasField prop1 = getStringScalar();
    prop1.setDefaultValue("defaultValue");
    OasField child1 = getObjectField(Map.of("prop1", prop1));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = ImmutableMap.of("prop2", "");
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"prop1\":\"defaultValue\"}}"));
  }


  @Test
  void map_returnsValue_forResponseWithDefaultScalarForEnvelopeTypeMismatch() throws NotFoundException {
    OasField prop1 = getStringScalar();
    prop1.setDefaultValue(1L);
    OasField child1 = getObjectField(Map.of("prop1", prop1));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = ImmutableMap.of("prop2", "");
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    assertThrows(MappingException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }


  @Test
  void map_returnsValue_forResponseWithDefaultArray() throws NotFoundException {
    OasField arrayObject1 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    OasField array1 = getArrayField(arrayObject1);
    array1.setDefaultValue(List.of("defaultvalue"));
    OasField child1 = getObjectField(Map.of("array1", array1));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"array1\":[\"defaultvalue\"]}}"));
  }


  @Test
  void map_returnsException_forResponseWithDefaultArrayWithInvalidType() throws NotFoundException {
    OasField arrayObject1 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    OasField array1 = getArrayField(arrayObject1);
    array1.setDefaultValue("defaultvalue");
    OasField child1 = getObjectField(Map.of("array1", array1));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    assertThrows(MappingException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }


  @Test
  void toResponse_returnsNoElement_forNonRequiredNonNillableEmptyArray() throws NotFoundException {
    OasField array = getArrayField(firstValue(REQUIRED_NILLABLE_STRING));
    array.setRequired(false);
    array.setNillable(false);
    OasField child1 = getObjectField(Map.of("array1", array));
    child1.setNillable(true);
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":null}"));
  }


  @Test
  void toResponse_returnsNoElement_forNonRequiredNullableNullArray() throws NotFoundException {
    OasField array = getArrayField(firstValue(REQUIRED_NON_NILLABLE_STRING));
    array.setRequired(false);
    array.setNillable(true);
    OasField child1 = getObjectField(Map.of("array1", array));
    child1.setNillable(true);
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()

        .oasField(rootField)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":null}"));
  }


  @Test
  void toResponse_returnsEmptyList_forRequiredNonNullableNullArray() throws NotFoundException {
    OasField array = getArrayField(firstValue(REQUIRED_NON_NILLABLE_STRING));
    array.setRequired(true);
    array.setNillable(false);
    OasField child1 = getObjectField(Map.of("array1", array));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .oasField(rootField)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"array1\":[]}}"));
  }


  @Test
  void toResponse_returnsNull_forRequiredNullableNullArray() throws NotFoundException {
    OasField array = getArrayField(firstValue(REQUIRED_NON_NILLABLE_STRING));
    array.setRequired(true);
    array.setNillable(true);
    OasField child1 = getObjectField(Map.of("array1", array));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = new HashMap<>();
    child1Data.put("array1", null);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()

        .oasField(rootField)
        .parameters(Collections.emptyMap())
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"array1\":null}}"));
  }


  @Test
  void toResponse_returnsDefaultValue_forInvalidScriptAndNullFallback() {
    OasField expressionScalar = getScalarField("string", "args.field1", null);
    expressionScalar.setDefaultValue("default");

    Object data = ImmutableMap.of(firstKey(REQUIRED_NILLABLE_STRING), "prop1value");
    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(data, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .uri(URI.create("http://dontcare.com:90210/bh?a=b"))
        .oasField(expressionScalar)
        .data(ImmutableMap.of(firstKey(REQUIRED_NILLABLE_STRING), "prop1value"))
        .dataStack(dataStack)
        .build();

    Object actual = jsonResponseMapper.mapScalarDataToResponse(writeContext, "root");

    assertEquals("default", actual);
  }


  @Test
  void map_returnsValue_forResponseWithObject() throws NotFoundException {
    OasField child2 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    OasField child1 = getObjectField(merge(Map.of("child2", child2), REQUIRED_NON_NILLABLE_STRING));
    OasField rootField = getObjectField(merge(Map.of("child1", child1), REQUIRED_NON_NILLABLE_STRING));

    Map<String, Object> child2Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    Map<String, Object> child1Data =
        ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v2", "child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v1", "child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()

        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"prop2\":\"v1\",\"child1\":{\"prop2\":\"v2\",\"child2\":{\"prop2\":\"v3\"}}}"));
  }


  @Test
  void map_returnsValue_withoutIncludedObject() {
    ResponseWriteContext writeContext = arrangeIncludeWriteContext("prop2 != `v3`");

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertEquals("{\"prop2\":\"v1\",\"child1\":{\"prop2\":\"v2\"}}", response);
  }


  @Test
  void map_returnsValue_withIncludedObject() {
    ResponseWriteContext writeContext = arrangeIncludeWriteContext("prop2 == `v3`");

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertEquals("{\"prop2\":\"v1\",\"child1\":{\"prop2\":\"v2\",\"child2\":{\"prop2\":\"v3\"}}}", response);
  }

  @Test
  void map_returnsNullObject_forEmptyObjectWithIdentifyingField() throws NotFoundException {
    OasField child1 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    child1.setNillable(true);
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = Collections.emptyMap();
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":null}"));
  }

  @Test
  void map_returnsObjectWithNullFields_forEmptyObjectWithoutIdentifyingField() throws NotFoundException {
    OasField child1 = getObjectField(REQUIRED_NILLABLE_STRING);
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = Collections.emptyMap();
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()

        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    String response = jsonResponseMapper.toResponse(writeContext)
        .block();

    assertTrue(response.contains("{\"child1\":{\"prop1\":null}}"));
  }

  @Test
  void map_throwsException_forMissingNonNillableRequiredField() throws NotFoundException {
    OasField child1 = getObjectField(merge(REQUIRED_NON_NILLABLE_STRING, REQUIRED_NILLABLE_STRING));
    OasField rootField = getObjectField(Map.of("child1", child1));

    Map<String, Object> child1Data = Map.of(firstKey(REQUIRED_NILLABLE_STRING), "v1");
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    ResponseWriteContext writeContext = ResponseWriteContext.builder()

        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    assertThrows(MappingException.class, () -> jsonResponseMapper.toResponse(writeContext));
  }

  @Test
  void validate_removeRoot_withDoubleValuePath() {
    String testString = "test.test";

    String resultString = jsonResponseMapper.removeRoot(testString);

    assertEquals("test", resultString);
  }

  @Test
  void validate_removeRoot_withMultivaluePath() {
    String testString = "test.test.test";

    String resultString = jsonResponseMapper.removeRoot(testString);

    assertEquals("test.test", resultString);
  }

  @Test
  void validate_removeRoot_withSinleValuePath() {
    String testString = "test";

    String resultString = jsonResponseMapper.removeRoot(testString);

    assertEquals("", resultString);
  }

  private ResponseWriteContext arrangeIncludeWriteContext(String condition) {

    OasField child2 = getObjectField(REQUIRED_NON_NILLABLE_STRING);
    child2.setRequired(false);
    ((OasObjectField) child2).setIncludeExpression(condition);
    OasField child1 = getObjectField(merge(Map.of("child2", child2), REQUIRED_NON_NILLABLE_STRING));

    OasField rootField = getObjectField(merge(Map.of("child1", child1), REQUIRED_NON_NILLABLE_STRING));

    Map<String, Object> child2Data = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v3");
    Map<String, Object> child1Data =
        ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v2", "child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of(firstKey(REQUIRED_NON_NILLABLE_STRING), "v1", "child1", child1Data);

    Deque<FieldContext> dataStack = new ArrayDeque<>();
    dataStack.push(createFieldContext(rootData, Collections.emptyMap()));

    return ResponseWriteContext.builder()
        .identifier("root")
        .oasField(rootField)
        .data(rootData)
        .dataStack(dataStack)
        .build();
  }

  private static OasField getObjectField(Map<String, OasField> children) {
    return new OasObjectField(false, true, children, false, null);
  }

  private static OasField getEnvelopeObjectField(Map<String, OasField> children) {
    return new OasObjectField(false, true, children, true, null);
  }

  private static OasField getArrayField(OasField content) {
    return new OasArrayField(false, true, content);
  }

  private static OasScalarField getStringScalar() {
    return new OasScalarField(false, true, "string");
  }

  private static OasScalarExpressionField getScalarField(String type, String expression, String fallbackValue) {
    return new OasScalarExpressionField(false, true, type, expression, fallbackValue);
  }

  private static Map<String, OasField> getScalarFieldMap(String identifier, String type, boolean required,
      boolean nillable, String dwsTemplate) {
    return Map.of(identifier, getScalarFieldMap(type, required, nillable, dwsTemplate, false));
  }

  private static OasField getScalarFieldMap(String type, boolean required, boolean nillable, String dwsTemplate,
      boolean envelope) {
    if (dwsTemplate != null) {
      return new OasScalarExpressionField(nillable, required, type, dwsTemplate, null);
    } else {
      return new OasScalarField(nillable, required, type);
    }
  }

  private static Map<String, OasField> merge(Map<String, OasField> map1, Map<String, OasField> map2) {
    var result = new HashMap<String, OasField>();
    result.putAll(map1);
    result.putAll(map2);
    return result;
  }

  private String firstKey(Map<String, ?> map) {
    return map.keySet()
        .iterator()
        .next();
  }

  private OasField firstValue(Map<?, OasField> map) {
    return map.values()
        .iterator()
        .next();
  }
}
