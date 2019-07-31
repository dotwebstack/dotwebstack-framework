package org.dotwebstack.framework.service.openapi.mapping;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.junit.jupiter.api.Test;

class ResponseMapperTest {
  private static final ResponseObject REQUIRED_NILLABLE_STRING = getProperty("prop1", "string", true, true);

  private static final ResponseObject REQUIRED_NON_NILLABLE_STRING = getProperty("prop2", "string", true, false);

  private static final ResponseObject NOT_REQUIRED_NILLABLE_STRING = getProperty("prop3", "string", false, true);

  private ResponseMapper responseMapper = new ResponseMapper();

  @Test
  @SuppressWarnings("unchecked")
  public void map_returnsProperty_ForValidResponse() {
    // Arrange
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NILLABLE_STRING));

    // Act
    Map<String, Object> response = (Map<String, Object>) responseMapper.mapResponse(responseObject,
        ImmutableMap.of(REQUIRED_NILLABLE_STRING.getIdentifier(), "prop1value"));

    // Assert
    assertTrue("prop1value".equals(response.get(REQUIRED_NILLABLE_STRING.getIdentifier())));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void map_omitsProperty_ForMissingNonRequiredProperty() {
    // Arrange
    ResponseObject responseObject = getObject("root", ImmutableList.of(NOT_REQUIRED_NILLABLE_STRING));

    // Act
    Map<String, Object> response =
        (Map<String, Object>) responseMapper.mapResponse(responseObject, ImmutableMap.of("another key", "prop1value"));

    // Assert
    assertTrue(response.isEmpty());
  }

  @Test
  public void map_throwsException_ForMissingRequiredNonNillableProperty() {
    // Arrange
    ResponseObject responseObject = getObject("root", ImmutableList.of(REQUIRED_NON_NILLABLE_STRING));

    // Act / Assert
    assertThrows(MappingException.class,
        () -> responseMapper.mapResponse(responseObject, ImmutableMap.of("other key", "prop1value")));
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
