package org.dotwebstack.framework.service.openapi.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.List;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResponseTemplateBuilderTest {

  private OpenAPI openApi;

  @BeforeEach
  public void setup() {
    this.openApi = TestResources.openApi();
  }

  @Test
  public void build_returnsTemplates_ForValidSpec() {
    // Arrange / Act
    List<ResponseTemplate> responseTemplates = getResponseTemplates(this.openApi, "/query1", "get");

    // Assert
    assertEquals(1, responseTemplates.size());
    ResponseTemplate okResponse = responseTemplates.stream()
        .filter(rt -> rt.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> new InvalidConfigurationException(""));

    assertEquals("application/hal+json", okResponse.getMediaType());
    assertEquals(200, okResponse.getResponseCode());
    assertEquals("object", okResponse.getResponseObject()
        .getType());
  }

  @Test
  public void build_throwsException_MissingOkResponse() {
    // Arrange
    ApiResponse response = openApi.getPaths()
        .get("/query1")
        .getGet()
        .getResponses()
        .get("200");
    openApi.getPaths()
        .get("/query1")
        .getGet()
        .getResponses()
        .remove("200");
    openApi.getPaths()
        .get("/query1")
        .getGet()
        .getResponses()
        .put("300", response);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> getResponseTemplates(this.openApi, "/query1", "get"));
  }

  @Test
  public void build_throwsException_MultipleOkResponses() {
    // Arrange
    ApiResponse response = openApi.getPaths()
        .get("/query1")
        .getGet()
        .getResponses()
        .get("200");
    openApi.getPaths()
        .get("/query1")
        .getGet()
        .getResponses()
        .put("205", response);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> getResponseTemplates(this.openApi, "/query1", "get"));
  }

  @Test
  public void build_throwsException_UnsupportedMediaType() {
    // Arrange
    Content content = openApi.getPaths()
        .get("/query1")
        .getGet()
        .getResponses()
        .get("200")
        .getContent();
    MediaType mediaType = content.get("application/hal+json");
    content.remove("application/hal+json");
    content.put("application/text", mediaType);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> getResponseTemplates(this.openApi, "/query1", "get"));
  }

  @Test
  public void build_throwsException_MultipleMediaTypes() {
    // Arrange
    Content content = openApi.getPaths()
        .get("/query1")
        .getGet()
        .getResponses()
        .get("200")
        .getContent();
    MediaType mediaType = content.get("application/hal+json");
    content.put("application/hal2+json", mediaType);

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> getResponseTemplates(this.openApi, "/query1", "get"));
  }

  @Test
  public void build_throwsException_MissingSchema() {
    // Arrange
    openApi.getComponents()
        .getSchemas()
        .remove("Object1");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> getResponseTemplates(this.openApi, "/query1", "get"));
  }

  @Test
  public void build_throwsException_MissingSchema2() {
    // Arrange
    openApi.getComponents()
        .getSchemas()
        .remove("Object2");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> getResponseTemplates(this.openApi, "/query1", "get"));
  }

  @Test
  public void build_resolvesXdwsTemplate_forValidSchema() {
    // Act
    List<ResponseTemplate> templates = getResponseTemplates(this.openApi, "/query1", "get");

    // Assert
    assertEquals(1, templates.size());
    ResponseTemplate responseTemplate = templates.get(0);
    assertEquals(1, responseTemplate.getResponseObject()
        .getChildren()
        .stream()
        .filter(ro -> "template_content".equals(ro.getDwsTemplate()))
        .count());
  }

  @Test
  public void build_throwsException_InvalidXdwsTemplateType() {
    // Arrange
    Schema<?> property1 = (Schema) openApi.getComponents()
        .getSchemas()
        .get("Object1")
        .getProperties()
        .get("o1_prop1");
    property1.setType("object");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> getResponseTemplates(this.openApi, "/query1", "get"));
  }


  public static List<ResponseTemplate> getResponseTemplates(OpenAPI openApi, String path, String methodName) {
    Operation operation = null;
    switch (methodName) {
      case "get":
        operation = openApi.getPaths()
            .get(path)
            .getGet();
        break;
      case "post":
        operation = openApi.getPaths()
            .get(path)
            .getPost();
        break;
      default:
        throw ExceptionHelper.unsupportedOperationException("method '{}' not yet supported.", methodName);
    }
    return new ResponseTemplateBuilder(openApi).buildResponseTemplates(path, methodName, operation);
  }
}
