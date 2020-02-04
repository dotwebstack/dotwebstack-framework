package org.dotwebstack.framework.service.openapi.param;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.requestbody.DefaultRequestBodyHandler;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestBodyHandlerTest {

  private OpenAPI openApi;

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private GraphQlField graphQlField;

  private DefaultRequestBodyHandler requestBodyHandler;

  private RequestBody requestBody;

  private RequestBodyContext requestBodyContext;

  @BeforeEach
  public void setup() {
    this.openApi = TestResources.openApi();
    this.typeDefinitionRegistry = TestResources.typeDefinitionRegistry();
    this.requestBodyHandler =
        new DefaultRequestBodyHandler(openApi, typeDefinitionRegistry, new Jackson2ObjectMapperBuilder());
    this.graphQlField = TestResources.getGraphQlField(this.typeDefinitionRegistry, "query4");
    this.requestBody = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody();
    this.requestBodyContext = new RequestBodyContext("object3", this.requestBody);
  }

  @Test
  public void validate_succeeds_forValidSchema() {
    // Act / Assert
    this.requestBodyHandler.validate(graphQlField, requestBody, "/query4");
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void validate_throwsException_forPropertyNotFoundInGraphQlQuery() {
    // Arrange
    Map<String, Schema> properties = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody()
        .getContent()
        .get("application" + "/json")
        .getSchema()
        .getProperties();
    Schema schema = properties.remove("object3");
    properties.put("object2", schema);

    RequestBody requestBody = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.requestBodyHandler.validate(graphQlField, requestBody, "/query4"));
  }

  @Test
  public void validate_throwsException_forPropertyNotFoundInGraphQlInput() {
    // Arrange
    this.typeDefinitionRegistry = TestResources.typeDefinitionRegistry("o3_prop1: String", "o3_prop3: String");
    this.requestBodyHandler =
        new DefaultRequestBodyHandler(openApi, typeDefinitionRegistry, new Jackson2ObjectMapperBuilder());

    RequestBody requestBody = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.requestBodyHandler.validate(graphQlField, requestBody, "/query4"));
  }

  @Test
  public void validate_throwsException_forGraphQlTypeMismatch() {
    // Arrange
    this.typeDefinitionRegistry = TestResources.typeDefinitionRegistry("o3_prop1: String", "o3_prop1: Boolean");
    this.requestBodyHandler =
        new DefaultRequestBodyHandler(openApi, typeDefinitionRegistry, new Jackson2ObjectMapperBuilder());

    RequestBody requestBody = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.requestBodyHandler.validate(graphQlField, requestBody, "/query4"));
  }

  @Test
  public void getValue_returns_Map_forValidJson() throws BadRequestException {

    // Arrange
    Map<String, Object> expected = new HashMap<>();
    ArrayList<String> expectedList = new ArrayList<>();
    expectedList.add("value1");
    expectedList.add("value2");
    expected.put("o3_prop2", expectedList);
    expected.put("o3_prop1", "value");
    ServerRequest serverRequest = mockServerRequest(
        "{ \"o3_prop1\" : \"value\", \"o3_prop2\" : [\"value1\", \"value2\"] }", MediaType.APPLICATION_JSON);

    // Act / Assert
    assertEquals(expected, this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  public void getValue_throwsException_forInvalidJson() {
    // Arrange
    ServerRequest serverRequest = mockServerRequest("test", MediaType.APPLICATION_JSON);

    // Act / Assert
    assertThrows(IllegalArgumentException.class,
        () -> this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  public void getValue_throwsException_unsupportedMediaType() {
    // Arrange
    ServerRequest serverRequest = mockServerRequest("test", MediaType.APPLICATION_PDF);

    // Act / Assert
    assertThrows(UnsupportedMediaTypeException.class,
        () -> this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  public void getValue_throwsException_emptyRequestBodyRequired() {
    // Arrange
    ServerRequest serverRequest = mockServerRequest(null, MediaType.APPLICATION_JSON);

    // Act / Assert
    assertThrows(BadRequestException.class,
        () -> this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  public void getValue_returnsEmpty_emptyRequestBodyNotRequired() throws BadRequestException {
    // Arrange
    this.requestBodyContext.getRequestBodySchema()
        .setRequired(Boolean.FALSE);
    ServerRequest serverRequest = mockServerRequest(null, MediaType.APPLICATION_JSON);

    // Act
    assertTrue(this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>())
        .isEmpty());
  }

  private ServerRequest mockServerRequest(String requestBodyContent, MediaType contentType) {
    ServerRequest serverRequest = Mockito.mock(ServerRequest.class);
    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.header(eq("Content-Type"))).thenReturn(Collections.singletonList(contentType.toString()));
    when(serverRequest.headers()).thenReturn(headers);

    Mono<String> mono;
    if (Objects.isNull(requestBodyContent)) {
      mono = Mono.empty();
    } else {
      mono = Mono.just(requestBodyContent);
    }
    when(serverRequest.bodyToMono(String.class)).thenReturn(mono);
    return serverRequest;
  }
}
