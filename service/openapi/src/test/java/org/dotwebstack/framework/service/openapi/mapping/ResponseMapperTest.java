package org.dotwebstack.framework.service.openapi.mapping;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.conversion.TypeConverterRouter;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@ExtendWith(MockitoExtension.class)
class ResponseMapperTest {
  private static final ResponseObject REQUIRED_NILLABLE_STRING =
      getProperty("prop1", "string", true, true, null, false);

  private static final ResponseObject REQUIRED_NON_NILLABLE_STRING =
      getProperty("prop2", "string", true, false, null, false);

  private static final ResponseObject NOT_REQUIRED_NILLABLE_STRING =
      getProperty("prop3", "string", false, true, null, false);

  private static final ResponseObject DWS_TEMPLATE = getProperty("prop4", "string", true, false,
      "`${env.env_var_1}_${fields.prop2}_${fields._parent.prop2}_${fields._parent._parent.prop2}`", false);

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  @Mock
  private EnvironmentProperties properties;

  private ResponseMapper responseMapper;

  @Mock
  private TypeConverterRouter typeConverterRouter;

  @BeforeEach
  public void setup() {
    this.responseMapper =
        new ResponseMapper(new Jackson2ObjectMapperBuilder(), jexlEngine, properties, typeConverterRouter);
  }

  @Test
  public void map_returnsProperty_ForValidResponse() throws NoResultFoundException, JsonProcessingException {
    // Arrange
    Object data = ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "prop1value");
    Deque<Object> dataStack = new ArrayDeque<>();
    dataStack.push(data);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(getObject("root", ImmutableList.of(REQUIRED_NILLABLE_STRING)))
        .data(ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "prop1value"))
        .dataStack(dataStack)
        .build();

    // Act
    String response = responseMapper.toJson(writeContext);

    // Assert
    assertTrue(response.equals("{\"prop1\":\"prop1value\"}"));
  }

  @Test
  public void map_returnsException_ForMissingRequiredProperty() {
    // Arrange
    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING)))
        .build();

    // Act & Assert
    assertThrows(NoResultFoundException.class, () -> responseMapper.toJson(writeContext));
  }

  @Test
  public void map_omitsProperty_ForMissingNonRequiredProperty() throws NoResultFoundException, JsonProcessingException {
    // Arrange
    Object data = ImmutableMap.of("another key", "prop1value");
    Deque<Object> dataStack = new ArrayDeque<>();
    dataStack.push(data);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(getObject("root", ImmutableList.of(NOT_REQUIRED_NILLABLE_STRING)))
        .data(data)
        .dataStack(dataStack)
        .build();

    // Act
    String response = responseMapper.toJson(writeContext);

    // Assert
    assertTrue(response.equals("{}"));
  }

  @Test
  public void map_throwsException_ForMissingRequiredNonNillableProperty() {
    // Arrange
    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING)))
        .data(ImmutableMap.of("other key", "prop1value"))
        .build();

    // Act / Assert
    assertThrows(MappingException.class, () -> responseMapper.toJson(writeContext));
  }

  @Test
  public void map_returnsValue_forDwsTemplate() throws NoResultFoundException, JsonProcessingException {
    // Arrange
    when(properties.getAllProperties()).thenReturn(ImmutableMap.of("env_var_1", "v0"));
    ResponseObject child2 = getObject("child2", ImmutableList.of(DWS_TEMPLATE));
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child2));
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2", "child2", child2Data);
    Map<String, Object> rootData =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v1", "child1", child1Data);

    Deque<Object> dataStack = new ArrayDeque<>();
    dataStack.push(rootData);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = responseMapper.toJson(writeContext);

    // Assert
    assertTrue(response.contains("\"prop4\":\"v0_v3_v2_v1\""));
  }

  @Test
  public void map_returnsValue_forResponseWithEnvelopeObjectValue()
      throws NoResultFoundException, JsonProcessingException {
    // Arrange
    ResponseObject child2 = getObject("child2", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject embedded = getEnvelopeObject("_embedded", ImmutableList.of(child2));
    ResponseObject child1 = getObject("child1", ImmutableList.of(embedded));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data = ImmutableMap.of("child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<Object> dataStack = new ArrayDeque<>();
    dataStack.push(rootData);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = responseMapper.toJson(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"_embedded\":{\"child2\":{\"prop2\":\"v3\"}}}}"));
  }

  @Test
  public void map_returnsValue_forResponseWithEmbeddedEnvelopeObjectValue()
      throws NoResultFoundException, JsonProcessingException {
    // Arrange
    ResponseObject child2 = getObject("child2", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject embedded1 = getEnvelopeObject("_embedded", ImmutableList.of(child2));
    ResponseObject embedded2 = getEnvelopeObject("_embedded", ImmutableList.of(embedded1));
    ResponseObject child1 = getObject("child1", ImmutableList.of(embedded2));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data = ImmutableMap.of("child2", child2Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<Object> dataStack = new ArrayDeque<>();
    dataStack.push(rootData);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = responseMapper.toJson(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"_embedded\":{\"_embedded\":{\"child2\":{\"prop2\":\"v3\"}}}}}"));
  }

  @Test
  public void map_returnsValue_forResponseWithArray() throws NoResultFoundException, JsonProcessingException {
    // Arrange
    ResponseObject arrayObject1 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject arrayObject2 = getObject("", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject array1 = getArrayObject("array1", ImmutableList.of(arrayObject1, arrayObject2));
    ResponseObject child1 = getObject("child1", ImmutableList.of(array1));
    ResponseObject responseObject = getObject("root", ImmutableList.of(child1));

    Map<String, Object> arrayObject1Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> arrayObject2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    List<Object> array1Data = ImmutableList.of(arrayObject1Data, arrayObject2Data);
    Map<String, Object> child1Data = ImmutableMap.of("array1", array1Data);
    Map<String, Object> rootData = ImmutableMap.of("child1", child1Data);

    Deque<Object> dataStack = new ArrayDeque<>();
    dataStack.push(rootData);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();


    // Act
    String response = responseMapper.toJson(writeContext);

    // Assert
    assertTrue(response.contains("{\"child1\":{\"array1\":[{\"prop2\":\"v3\"},{\"prop2\":\"v3\"}]}}"));
  }

  @Test
  public void map_returnsValue_forResponseWithObject() throws NoResultFoundException, JsonProcessingException {
    ResponseObject child2 = getObject("child2", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child2));
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child1));

    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2", "child2", child2Data);
    Map<String, Object> rootData =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v1", "child1", child1Data);

    Deque<Object> dataStack = new ArrayDeque<>();
    dataStack.push(rootData);

    ResponseWriteContext writeContext = ResponseWriteContext.builder()
        .schema(responseObject)
        .data(rootData)
        .dataStack(dataStack)
        .build();

    // Act
    String response = responseMapper.toJson(writeContext);

    // Assert
    assertTrue(response.contains("{\"prop2\":\"v1\",\"child1\":{\"prop2\":\"v2\",\"child2\":{\"prop2\":\"v3\"}}}"));
  }

  private static ResponseObject getArrayObject(String identifier, List<ResponseObject> items) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type("array")
        .items(items)
        .isEnvelope(false)
        .build();
  }

  private static ResponseObject getEnvelopeObject(String identifier, List<ResponseObject> children) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type("object")
        .children(children)
        .isEnvelope(true)
        .build();
  }

  private static ResponseObject getObject(String identifier, List<ResponseObject> children) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type("object")
        .children(children)
        .build();
  }

  private static ResponseObject getProperty(String identifier, String type, boolean required, boolean nillable,
      String dwsTemplate, boolean envelope) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type(type)
        .required(required)
        .nillable(nillable)
        .dwsTemplate(dwsTemplate)
        .isEnvelope(envelope)
        .build();
  }
}
