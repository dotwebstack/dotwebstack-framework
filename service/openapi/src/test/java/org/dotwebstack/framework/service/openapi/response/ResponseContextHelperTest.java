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
    ResponseObject root = buildResponseObject("key", "string", false, null);
    ResponseTemplate responseTemplate = ResponseTemplate.builder()
        .responseObject(root)
        .responseCode(200)
        .build();
    List<ResponseTemplate> responses = Collections.singletonList(responseTemplate);

    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    ResponseSchemaContext responseSchemaContext = ResponseSchemaContext.builder()
        .graphQlField(graphQlField)
        .responses(responses)
        .build();
    Map<String, Object> inputParams = Collections.emptyMap();

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

    ResponseSchemaContext responseSchemaContext = ResponseSchemaContext.builder()
        .graphQlField(graphQlField)
        .responses(responses)
        .build();
    Map<String, Object> inputParams = Collections.emptyMap();

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

    ResponseSchemaContext responseSchemaContext = ResponseSchemaContext.builder()
        .graphQlField(graphQlField)
        .responses(responses)
        .build();
    Map<String, Object> inputParams = ImmutableMap.of("key", "value");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> ResponseContextHelper.getPathsForSuccessResponse(responseSchemaContext, inputParams));
  }

  @Test
  void validate_getRequiredResponseObject_withNoRequiredFields() {
    // Arrange
    ResponseObject root = buildResponseObject("key", "string", false, null);
    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, graphQlField, Collections.emptyMap());

    // Assert
    assertEquals(0, responseObject.entrySet()
        .size());
  }

  @Test
  void validate_getRequiredResponseObject_withOneRequiredField() {
    // Arrange
    ResponseObject root = buildResponseObject("key", "string", true, null);
    GraphQlField graphQlField = buildGraphQlField("key", Collections.emptyList());

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, graphQlField, Collections.emptyMap());

    // Assert
    assertEquals(0, responseObject.entrySet()
        .size());
  }

  @Test
  void validate_getRequiredResponseObject_withChildFields() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true, null);
    ResponseObject child = buildResponseObject("child", "string", true, root);
    root.getSummary()
        .setChildren(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, Collections.emptyMap());

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("child"), is(equalTo(child.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withRequiredPaths() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true, null);
    ResponseObject child = buildResponseObject("child", "object", true, root);
    ResponseObject grandChild = buildResponseObject("grandchild", "string", false, child);
    child.getSummary()
        .setChildren(List.of(grandChild));
    root.getSummary()
        .setChildren(List.of(child));

    ResponseTemplate template = ResponseTemplate.builder()
        .responseCode(200)
        .responseObject(root)
        .build();

    GraphQlField rootField = GraphQlField.builder()
        .name("root")
        .fields(List.of(GraphQlField.builder()
            .name("child")
            .fields(List.of(GraphQlField.builder()
                .name("grandchild")
                .build()))
            .build()))
        .build();

    ResponseSchemaContext context = ResponseSchemaContext.builder()
        .graphQlField(rootField)
        .responses(List.of(template))
        .requiredFields(List.of("child.grandchild"))
        .build();

    // Act
    Set<String> paths = ResponseContextHelper.getPathsForSuccessResponse(context, Collections.emptyMap());

    // Assert
    assertEquals(2, paths.size());
    assertTrue(paths.contains("child"));
    assertTrue(paths.contains("child.grandchild"));
  }

  @Test
  void validate_getRequiredResponseObject_forComplexObject() {
    // Arrange
    ResponseObject query = buildResponseObject("query", "object", true, true, null);
    ResponseObject breweriesArray = buildResponseObject("breweries", "array", true, query);
    ResponseObject breweriesObject = buildResponseObject("breweries", "object", true, breweriesArray);
    ResponseObject breweryIdentifier = buildResponseObject("identifier", "string", true, breweriesObject);
    ResponseObject ownerObject = buildResponseObject("owner", "object", true, true, breweriesObject);
    ResponseObject ownerName = buildResponseObject("ownerName", "string", true, ownerObject);
    ResponseObject beersArray = buildResponseObject("beers", "array", true, breweriesObject);
    ResponseObject beersObject = buildResponseObject("beers", "object", true, beersArray);
    ResponseObject beerIdentifier = buildResponseObject("identifier", "object", true, beersObject);

    query.getSummary()
        .setChildren(List.of(breweriesArray));
    breweriesArray.getSummary()
        .setItems(List.of(breweriesObject));
    breweriesObject.getSummary()
        .setChildren(List.of(breweryIdentifier, ownerObject, beersArray));
    ownerObject.getSummary()
        .setChildren(List.of(ownerName));
    beersArray.getSummary()
        .setItems(List.of(beersObject));
    beersObject.getSummary()
        .setChildren(List.of(beerIdentifier));

    ResponseTemplate template = ResponseTemplate.builder()
        .responseCode(200)
        .responseObject(query)
        .build();

    GraphQlField rootField = GraphQlField.builder()
        .name("brewery")
        .fields(List.of(GraphQlField.builder()
            .name("ownerName")
            .build()))
        .fields(List.of(GraphQlField.builder()
            .name("identifier")
            .build()))
        .fields(List.of(GraphQlField.builder()
            .name("beers")
            .fields(List.of(GraphQlField.builder()
                .name("identifier")
                .build()))
            .build()))
        .type("Brewery")
        .build();

    ResponseSchemaContext context = ResponseSchemaContext.builder()
        .graphQlField(rootField)
        .responses(List.of(template))
        .build();

    // Act
    Set<String> paths = ResponseContextHelper.getPathsForSuccessResponse(context, Collections.emptyMap());

    // Assert
    assertEquals(4, paths.size());
    assertTrue(paths.contains("identifier"));
    assertTrue(paths.contains("beers"));
    assertTrue(paths.contains("ownerName"));
    assertTrue(paths.contains("beers.identifier"));
  }

  @Test
  void validate_getRequiredResponseObject_withComposedOfFields() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true, null);
    ResponseObject child = buildResponseObject("child", "string", true, root);
    root.getSummary()
        .setComposedOf(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, Collections.emptyMap());

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("child"), is(equalTo(child.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withComposedOfObjectField() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true, null);
    ResponseObject child = buildResponseObject("child", "object", true, root);
    root.getSummary()
        .setComposedOf(List.of(child));
    ResponseObject grandchild = buildResponseObject("grandchild", "string", true, root);
    child.getSummary()
        .setChildren(List.of(grandchild));

    GraphQlField rootField = buildGraphQlField("root", List.of("grandchild"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, Collections.emptyMap());

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("grandchild"), is(equalTo(grandchild.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withMultiPathComposedOfFields() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true, null);
    ResponseObject child = buildResponseObject("child", "string", true, root);
    root.getSummary()
        .setComposedOf(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, Collections.emptyMap());

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("child"), is(equalTo(child.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withListDirectlyUnderRootObject() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true, null);
    ResponseObject children = buildResponseObject("children", "array", true, root);
    root.getSummary()
        .setChildren(List.of(children));
    ResponseObject child = buildResponseObject("children", "object", true, children);
    children.getSummary()
        .setItems(List.of(child));
    ResponseObject identifier = buildResponseObject("identifier", "object", true, child);
    child.getSummary()
        .setChildren(List.of(identifier));

    GraphQlField rootField = GraphQlField.builder()
        .name("root")
        .fields(List.of(GraphQlField.builder()
            .name("children")
            .fields(List.of(GraphQlField.builder()
                .name("identifier")
                .build()))
            .build()))
        .build();

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, Collections.emptyMap());

    // Assert
    assertEquals(2, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("children"), is(equalTo(child.getSummary())));
    assertThat(responseObject.get("children.identifier"), is(equalTo(identifier.getSummary())));
  }

  @Test
  void validate_getRequiredResponseObject_withItems() {
    // Arrange
    ResponseObject root = buildResponseObject("root", "object", true, null);
    ResponseObject child = buildResponseObject("child", "string", true, root);
    root.getSummary()
        .setItems(List.of(child));

    GraphQlField rootField = buildGraphQlField("root", List.of("child"));

    // Act
    Map<String, SchemaSummary> responseObject =
        ResponseContextHelper.getRequiredResponseObject("", root, rootField, Collections.emptyMap());

    // Assert
    assertEquals(1, responseObject.entrySet()
        .size());
    assertThat(responseObject.get("child"), is(equalTo(child.getSummary())));
  }

  @Test
  public void validate_isExpanded_withExpandedParameter() {
    assertTrue(ResponseContextHelper.isExpanded(ImmutableMap.of(X_DWS_EXPANDED_PARAMS, List.of("test")), "test"));
  }

  @Test
  public void validate_isExpanded_withUnExpandedParameter() {
    assertFalse(ResponseContextHelper.isExpanded(Collections.emptyMap(), ""));
  }

  private ResponseObject buildResponseObject(String name, String type, boolean required, ResponseObject parent) {
    return buildResponseObject(name, type, required, false, parent);
  }

  private ResponseObject buildResponseObject(String name, String type, boolean required, boolean envelope,
      ResponseObject parent) {
    return ResponseObject.builder()
        .identifier(name)
        .parent(parent)
        .summary(SchemaSummary.builder()
            .type(type)
            .isEnvelope(envelope)
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
