package org.dotwebstack.framework.service.openapi;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_QUERY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import graphql.GraphQL;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.util.Collections;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.RequestPredicate;

@ExtendWith(MockitoExtension.class)
public class OpenApiConfigurationTest {

  @Mock
  private GraphQL graphQL;

  private TypeDefinitionRegistry registry;

  private OpenAPI openApi;

  private OpenApiConfiguration openApiConfiguration;

  @Mock
  private ResponseContextValidator responseContextValidator;

  @Mock
  private ResponseMapper responseMapper;

  @BeforeEach
  public void setup() {
    this.registry = TestResources.typeDefinitionRegistry();
    this.openApi = TestResources.openApi();
    this.openApiConfiguration = spy(new OpenApiConfiguration(graphQL, this.registry, responseMapper,
        new ParamHandlerRouter(Collections.emptyList(), openApi), responseContextValidator));
  }

  @Test
  public void route_returnsFunctions() {
    // Act
    openApiConfiguration.route(openApi);

    // Assert
    verify(this.openApiConfiguration, atLeastOnce()).toRouterFunction(any(ResponseTemplateBuilder.class), eq("/query1"),
        any(GraphQlField.class), eq("get"), any(Operation.class),
        argThat(new RequestPredicateMatcher("(GET && " + "/query1)")));
    verify(this.openApiConfiguration).toRouterFunction(any(ResponseTemplateBuilder.class), eq("/query1"),
        any(GraphQlField.class), eq("post"), any(Operation.class),
        argThat(new RequestPredicateMatcher("(POST && " + "/query1)")));
    verify(this.openApiConfiguration, atLeastOnce()).toRouterFunction(any(ResponseTemplateBuilder.class), eq("/query2"),
        any(GraphQlField.class), eq("get"), any(Operation.class),
        argThat(new RequestPredicateMatcher("(GET && " + "/query2)")));
  }

  @Test
  public void route_throwsException_MissingQuery() {
    // Arrange
    openApi.getPaths()
        .get("/query1")
        .getGet()
        .getExtensions()
        .put(X_DWS_QUERY, "unknownQuery");

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> openApiConfiguration.route(openApi));
  }

  private static class RequestPredicateMatcher implements ArgumentMatcher<RequestPredicate> {

    private final String expectedString;

    public RequestPredicateMatcher(String expectdString) {
      this.expectedString = expectdString;
    }

    @Override
    public boolean matches(RequestPredicate requestPredicate) {
      return requestPredicate != null && requestPredicate.toString()
          .equals(expectedString);
    }
  }
}
