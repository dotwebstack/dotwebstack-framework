package org.dotwebstack.framework.service.openapi.mapping;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@ExtendWith(MockitoExtension.class)
class ResponseMapperTest {
  private static final ResponseObject REQUIRED_NILLABLE_STRING = getProperty("prop1", "string", true, true, null);

  private static final ResponseObject REQUIRED_NON_NILLABLE_STRING = getProperty("prop2", "string", true, false, null);

  private static final ResponseObject NOT_REQUIRED_NILLABLE_STRING = getProperty("prop3", "string", false, true, null);

  private static final ResponseObject DWS_TEMPLATE = getProperty("prop4", "string", true, false,
      "`${env.env_var_1}_${fields.prop2}_${fields._parent.prop2}_${fields._parent._parent.prop2}`");

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  @Mock
  private EnvironmentProperties properties;

  private ResponseMapper responseMapper;

  @BeforeEach
  public void setup() {
    this.responseMapper = new ResponseMapper(new Jackson2ObjectMapperBuilder(), jexlEngine, properties);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void map_returnsProperty_ForValidResponse() throws NoResultFoundException, JsonProcessingException {
    // Arrange
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NILLABLE_STRING));

    // Act
    String response =
        responseMapper.toJson(responseObject, ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "prop1value"));

    // Assert
    assertTrue(response.equals("{\"prop1\":\"prop1value\"}"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void map_returnsException_ForMissingRequiredProperty() {
    // Arrange
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));

    // Act & Assert
    assertThrows(NoResultFoundException.class, () -> responseMapper.toJson(responseObject, null));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void map_omitsProperty_ForMissingNonRequiredProperty() throws NoResultFoundException, JsonProcessingException {
    // Arrange
    ResponseObject responseObject = getObject("root", ImmutableList.of(NOT_REQUIRED_NILLABLE_STRING));

    // Act
    String response = responseMapper.toJson(responseObject, ImmutableMap.of("another key", "prop1value"));

    // Assert
    assertTrue(response.equals("{}"));
  }

  @Test
  public void map_throwsException_ForMissingRequiredNonNillableProperty() {
    // Arrange
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));

    // Act / Assert
    assertThrows(MappingException.class,
        () -> responseMapper.toJson(responseObject, ImmutableMap.of("other key", "prop1value")));
  }

  @Test
  public void map_returnsValue_forDwsTemplate() throws NoResultFoundException, JsonProcessingException {
    // Arrange
    when(properties.getAllProperties()).thenReturn(ImmutableMap.of("env_var_1", "v0"));
    ResponseObject child2 = getObject("child2", ImmutableList.of(DWS_TEMPLATE));
    ResponseObject child1 = getObject("child1", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child2));
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING, child1));

    // Act
    Map<String, Object> child2Data = ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v3");
    Map<String, Object> child1Data =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v2", "child2", child2Data);
    Map<String, Object> rootData =
        ImmutableMap.of(REQUIRED_NON_NILLABLE_STRING.getIdentifier(), "v1", "child1", child1Data);
    String response = responseMapper.toJson(responseObject, rootData);

    // Assert
    assertTrue(response.contains("\"prop4\":\"v0_v3_v2_v1\""));
  }

  private static ResponseObject getObject(String identifier, List<ResponseObject> children) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type("object")
        .children(children)
        .build();
  }

  private static ResponseObject getProperty(String identifier, String type, boolean required, boolean nillable,
      String dwsTemplate) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type(type)
        .required(required)
        .nillable(nillable)
        .dwsTemplate(dwsTemplate)
        .build();
  }
}
