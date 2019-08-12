package org.dotwebstack.framework.service.openapi.mapping;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class ResponseMapperTest {
  private static final ResponseObject REQUIRED_NILLABLE_STRING = getProperty("prop1", "string", true, true);

  private static final ResponseObject REQUIRED_NON_NILLABLE_STRING = getProperty("prop2", "string", true, false);

  private static final ResponseObject NOT_REQUIRED_NILLABLE_STRING = getProperty("prop3", "string", false, true);

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private ResponseMapper responseMapper = new ResponseMapper(new Jackson2ObjectMapperBuilder(), jexlEngine, null);

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

  private static ResponseObject getObject(String identifier, List<ResponseObject> children) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type("object")
        .children(children)
        .build();
  }

  private static ResponseObject getProperty(String identifier, String type, boolean required, boolean nillable) {
    return ResponseObject.builder()
        .identifier(identifier)
        .type(type)
        .required(required)
        .nillable(nillable)
        .build();
  }
}
