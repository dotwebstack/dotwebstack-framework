package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.junit.jupiter.api.Test;

class ResponseContextHelperTest {

  @Test
  void validate_getPathsForSuccessResponse_withNoRequiredFields() {
    // Arrange
    ResponseObject root = buildResponseObject("key", "string", false);
    ResponseTemplate responseTemplate = ResponseTemplate.builder()
        .responseObject(root)
        .responseCode(200)
        .build();
    List<ResponseTemplate> responses = Collections.singletonList(responseTemplate);

    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    ResponseSchemaContext responseSchemaContext = new ResponseSchemaContext(graphQlField, responses, null, null, null);
    Map<String, Object> inputParams = ImmutableMap.of("key", "value");

    // Act
    Set<String> paths = ResponseContextHelper.getPathsForSuccessResponse(responseSchemaContext, inputParams);

    // Assert
    assertEquals(0, paths.size());
  }

  @Test
  void validate_getPathsForSuccessResponse_withNoResponseObject() {
    // Arrange
    ResponseTemplate responseTemplate = ResponseTemplate.builder()
        .responseCode(200)
        .build();
    List<ResponseTemplate> responses = Collections.singletonList(responseTemplate);

    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    ResponseSchemaContext responseSchemaContext = new ResponseSchemaContext(graphQlField, responses, null, null, null);
    Map<String, Object> inputParams = ImmutableMap.of("key", "value");

    // Act
    Set<String> paths = ResponseContextHelper.getPathsForSuccessResponse(responseSchemaContext, inputParams);

    // Assert
    assertEquals(0, paths.size());
  }

  @Test
  void validate_throwsInvalidConfigurationException_withNoResponseTemplate() {
    // Arrange
    List<ResponseTemplate> responses = Collections.emptyList();

    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    ResponseSchemaContext responseSchemaContext = new ResponseSchemaContext(graphQlField, responses, null, null, null);
    Map<String, Object> inputParams = ImmutableMap.of("key", "value");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> ResponseContextHelper.getPathsForSuccessResponse(responseSchemaContext, inputParams));
  }

  @Test
  void validate_getRequiredResponseObject_withNoRequiredFields() {
    // Arrange
    ResponseObject root = buildResponseObject("key", "string", false);
    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, graphQlField, ImmutableMap.of("key", "value"), false);

    // Assert
    assertEquals(0, responseObject.entrySet()
        .size());
  }

  @Test
  void validate_getRequiredResponseObject_withOneRequiredField() {
    // Arrange
    ResponseObject root = buildResponseObject("key", "string", true);
    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, graphQlField, ImmutableMap.of("key", "value"), false);

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("key"), is(equalTo(root.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withChildFields() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true);
    ResponseObject child = buildResponseObject("child", "string", true);
    root.getSummary()
        .setChildren(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, ImmutableMap.of("child", "value"), false);

    // Assert
    assertEquals(2, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("root"), is(equalTo(root.getSummary())));
    assertThat(responseObject.get("root.child"), is(equalTo(child.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withComposedOfFields() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true);
    ResponseObject child = buildResponseObject("child", "string", true);
    root.getSummary()
        .setComposedOf(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, ImmutableMap.of("child", "value"), false);

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("child"), is(equalTo(child.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withMultiPathComposedOfFields() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true);
    ResponseObject child = buildResponseObject("child", "string", true);
    root.getSummary()
        .setComposedOf(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject = ResponseContextHelper.getRequiredResponseObject("root.root", root,
        rootField, ImmutableMap.of("child", "value"), false);

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("root.child"), is(equalTo(child.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withItems() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true);
    ResponseObject child = buildResponseObject("child", "string", true);
    root.getSummary()
        .setItems(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, ImmutableMap.of("child", "value"), false);

    // Assert
    assertEquals(2, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("root"), is(equalTo(root.getSummary())));
    assertThat(responseObject.get("root.child"), is(equalTo(child.getSummary())));
  }

  @Test
  public void validate_isExpanded_withExpandedParameter() {
    assertTrue(ResponseContextHelper.isExpanded(ImmutableMap.of(X_DWS_EXPANDED_PARAMS, List.of("test")), "test"));
  }

  @Test
  public void validate_isExpanded_withUnExpandedParameter() {
    assertFalse(ResponseContextHelper.isExpanded(Collections.emptyMap(), ""));
  }

  private ResponseObject buildResponseObject(String name, String type, boolean required) {
    return ResponseObject.builder()
        .identifier(name)
        .summary(SchemaSummary.builder()
            .type(type)
            .required(required)
            .build())
        .build();
  }

  private GraphQlField buildGraphQlField(String name, List<String> fieldNames) {
    return GraphQlField.builder()
        .name(name)
        .fields(fieldNames.stream()
            .map(fieldName -> GraphQlField.builder()
                .name(fieldName)
                .build())
            .collect(Collectors.toList()))
        .build();
  }

}
