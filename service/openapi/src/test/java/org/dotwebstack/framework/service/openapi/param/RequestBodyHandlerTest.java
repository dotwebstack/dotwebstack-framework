package org.dotwebstack.framework.service.openapi.param;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class RequestBodyHandlerTest {

  private OpenAPI openApi;

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private GraphQlField graphQlField;

  private RequestBodyHandler requestBodyHandler;

  @BeforeEach
  public void setup() {
    this.openApi = TestResources.openApi();
    this.typeDefinitionRegistry = TestResources.typeDefinitionRegistry();
    this.requestBodyHandler = new RequestBodyHandler(openApi, typeDefinitionRegistry);
    this.graphQlField = TestResources.getGraphQlField(this.typeDefinitionRegistry, "query4");
  }

  @Test
  public void validate_succeeds_forValidSchema() {
    // Arrange
    RequestBody requestBody = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody();

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
    this.requestBodyHandler = new RequestBodyHandler(openApi, typeDefinitionRegistry);

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
    this.requestBodyHandler = new RequestBodyHandler(openApi, typeDefinitionRegistry);

    RequestBody requestBody = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> this.requestBodyHandler.validate(graphQlField, requestBody, "/query4"));
  }

  @Test
  public void getValue_returns_Map_forValidJson() {
    // Arrange
    ServerRequest serverRequest = Mockito.mock(ServerRequest.class);
    Mono<String> mono = Mono.just("{ \"o3_prop1\" : \"value\", \"o3_prop2\" : [\"value1\", \"value2\"] }");
    when(serverRequest.bodyToMono(String.class)).thenReturn(mono);

    // Act
    Optional<Object> value = this.requestBodyHandler.getValue(serverRequest);

    // Assert
    assertTrue(value.isPresent());

    Map<String, Object> expected = new HashMap<>();
    ArrayList<String> expectedList = new ArrayList<>();
    expectedList.add("value1");
    expectedList.add("value2");
    expected.put("o3_prop2", expectedList);
    expected.put("o3_prop1", "value");

    assertEquals(expected, value.get());
  }

  @Test
  public void getValue_throwsException_forInvalidJson() {
    // Arrange
    ServerRequest serverRequest = Mockito.mock(ServerRequest.class);
    Mono<String> mono = Mono.just("test");
    when(serverRequest.bodyToMono(String.class)).thenReturn(mono);

    // Act / Assert
    assertThrows(IllegalArgumentException.class, () -> this.requestBodyHandler.getValue(serverRequest));
  }
}
