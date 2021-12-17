package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.TestConstants.APPLICATION_JSON_HAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.jexl3.JexlBuilder;
import org.dotwebstack.framework.service.openapi.OpenApiProperties;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.jexl.PagingFunctions;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.GeometryTypeMapper;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class JsonBodyMapperTest {

  private OpenAPI openApi;

  private JsonBodyMapper bodyMapper;

  @Mock
  private EnvironmentProperties properties;

  @BeforeEach
  void beforeEach() {
    openApi = TestResources.openApi("openapi.yaml");
    var pagingLinkFunctions = new PagingFunctions();
    bodyMapper = new JsonBodyMapper(TestResources.graphQlSchema(), new JexlBuilder().silent(false)
        .strict(true)
        .namespaces(Map.of(pagingLinkFunctions.getNamespace(), pagingLinkFunctions))
        .create(), properties, List.of(new GeometryTypeMapper(new OpenApiProperties())));
  }

  static Stream<Arguments> arguments() {
    return Stream.of(Arguments.of("/breweries", APPLICATION_JSON, Map.of(), "brewery-collection", "breweries-json"),
        Arguments.of("/breweries", APPLICATION_JSON_HAL, Map.of(), "brewery-collection", "breweries-json-hal"),
        Arguments.of("/breweries-pageable", APPLICATION_JSON, Map.of(), "brewery-pageable-collection",
            "breweries-json"),
        Arguments.of("/breweries-pageable", APPLICATION_JSON_HAL,
            Map.of("requestPathAndQuery", "/breweries", "pageSize", 10), "brewery-pageable-collection",
            "breweries-json-hal-no-next"),
        Arguments.of("/breweries-pageable", APPLICATION_JSON_HAL,
            Map.of("requestPathAndQuery", "/breweries", "pageSize", 10), "brewery-pageable-collection",
            "breweries-json-hal-no-next"),
        Arguments.of("/breweries-pageable", APPLICATION_JSON_HAL,
            Map.of("requestPathAndQuery", "/breweries", "pageSize", 3), "brewery-pageable-collection",
            "breweries-json-hal-next"),
        Arguments.of("/breweries-pageable", APPLICATION_JSON_HAL,
            Map.of("requestPathAndQuery", "/breweries?page=2", "pageSize", 3), "brewery-pageable-collection",
            "breweries-json-hal-next-prev"),
        Arguments.of("/brewery/{identifier}", APPLICATION_JSON, Map.of("identifier", "foo"), "brewery", "brewery-json"),
        Arguments.of("/brewery/{identifier}", APPLICATION_JSON_HAL, Map.of("identifier", "foo"), "brewery",
            "brewery-json-hal"),
        Arguments.of("/brewery-nulls/{identifier}", APPLICATION_JSON, Map.of("identifier", "foo"), "brewery",
            "brewery-json-nulls"),
        Arguments.of("/brewery-required-empty-nullable-envelope/{identifier}", APPLICATION_JSON,
            Map.of("identifier", "foo"), "brewery", "brewery-json-required-empty-nullable-envelope"),
        Arguments.of("/brewery-rename-field/{identifier}", APPLICATION_JSON, Map.of("identifier", "foo"), "brewery",
            "brewery-json-translated"));
  }

  @ParameterizedTest
  @MethodSource("arguments")
  void map(String path, MediaType preferredMediaType, Map<String, Object> parameters, String graphQlResult,
      String expectedBody) {

    when(properties.getAllProperties()).thenReturn(Map.of("baseUrl", "https://dotwebstack.org/api"));

    var operationRequest = OperationRequest.builder()
        .context(createOperationContext(path))
        .preferredMediaType(preferredMediaType)
        .parameters(parameters)
        .build();

    Map<String, Object> data = TestResources.graphQlResult(graphQlResult)
        .getData();

    var result = data.get(operationRequest.getContext()
        .getQueryProperties()
        .getField());

    StepVerifier.create(bodyMapper.map(operationRequest, result))
        .assertNext(body -> assertThat(body, is(equalTo(TestResources.body(expectedBody)))))
        .verifyComplete();
  }

  @Test
  void supports_returnsTrue_forJsonTypeOnly() {
    var operationContext = createOperationContext("/breweries");

    assertThat(bodyMapper.supports(APPLICATION_JSON, operationContext), is(true));
    assertThat(bodyMapper.supports(APPLICATION_JSON_HAL, operationContext), is(true));
  }

  private OperationContext createOperationContext(String path) {
    var operation = openApi.getPaths()
        .get(path)
        .getGet();

    return OperationContext.builder()
        .operation(operation)
        .successResponse(MapperUtils.getSuccessResponse(operation))
        .queryProperties(QueryProperties.fromOperation(operation))
        .build();
  }
}
