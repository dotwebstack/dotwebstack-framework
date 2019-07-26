package org.dotwebstack.framework.service.openapi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.GraphQL;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@ExtendWith(MockitoExtension.class)
public class OpenApiConfigurationTest {

  @Mock
  private GraphQL graphQL;

  @Mock
  private OpenApiProperties openApiProperties;

  private TypeDefinitionRegistry registry;

  private OpenAPI openApi;

  private OpenApiConfiguration openApiConfiguration;



  @BeforeEach
  public void setup() {
    this.registry = TestResources.typeDefinitionRegistry();
    this.openApi = TestResources.openApi();
    this.openApiConfiguration =
        new OpenApiConfiguration(graphQL, this.registry, new Jackson2ObjectMapperBuilder(), openApiProperties);
  }

  @Test
  public void route_returnsFunctions() {
    // Act
    RouterFunction<ServerResponse> functions = openApiConfiguration.route(openApi);

    // Assert
    assertNotNull(functions);
  }

  @Test
  public void route_throwsException_MissingQuery() {
    // Arrange
    openApi.getPaths()
        .get("/query1")
        .getGet()
        .getExtensions()
        .put("x-dws-query", "unknownQuery");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> openApiConfiguration.route(openApi));
  }
}
