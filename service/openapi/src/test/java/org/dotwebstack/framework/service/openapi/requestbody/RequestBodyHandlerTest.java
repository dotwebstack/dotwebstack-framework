package org.dotwebstack.framework.service.openapi.requestbody;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import graphql.language.ListType;
import graphql.language.Type;
import graphql.language.TypeName;
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
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
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
class RequestBodyHandlerTest {

  private OpenAPI openApi;

  private DefaultRequestBodyHandler requestBodyHandler;

  private RequestBody requestBody;

  private RequestBodyContext requestBodyContext;

  @BeforeEach
  void setup() {
    this.openApi = TestResources.openApi();
    this.requestBodyHandler =
        new DefaultRequestBodyHandler(openApi, new Jackson2ObjectMapperBuilder());
    this.requestBody = this.openApi.getPaths()
        .get("/query4")
        .getGet()
        .getRequestBody();
    this.requestBodyContext = new RequestBodyContext(this.requestBody);
  }

  @Test
  void getValue_returns_Map_forValidJson() throws BadRequestException {
    Map<String, Object> expected = new HashMap<>();
    ArrayList<String> expectedList = new ArrayList<>();
    expectedList.add("value1");
    expectedList.add("value2");
    expected.put("o3_prop2", expectedList);
    expected.put("o3_prop1", "value");
    ServerRequest serverRequest = mockServerRequest(
        "{ \"o3_prop1\" : \"value\", \"o3_prop2\" : [\"value1\", \"value2\"] }", MediaType.APPLICATION_JSON);

    assertEquals(expected,
        this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  void getValue_throwsException_forInvalidJson() {
    ServerRequest serverRequest = mockServerRequest("test", MediaType.APPLICATION_JSON);

    assertThrows(BadRequestException.class,
        () -> this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  void getValue_throwsException_unsupportedMediaType() {
    ServerRequest serverRequest = mockServerRequest("test", MediaType.APPLICATION_PDF);

    Map<String, Object> parameterMap = new HashMap<>();

    assertThrows(UnsupportedMediaTypeException.class,
        () -> this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, parameterMap));
  }

  @Test
  void getValue_throwsNoException_forSupportedMediaTypeWithSuffix() {
    ServerRequest serverRequest =
        mockServerRequest("{ \"foo\" : \"bar\"}", MediaType.parseMediaType("application/json;charset=utf-8"));

    assertDoesNotThrow(
        () -> this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  void getValue_throwsException_emptyRequestBodyRequired() {
    ServerRequest serverRequest = mockServerRequest(null, MediaType.APPLICATION_JSON);

    assertThrows(BadRequestException.class,
        () -> this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>()));
  }

  @Test
  void getValue_returnsEmpty_emptyRequestBodyNotRequired() throws BadRequestException {
    this.requestBodyContext.getRequestBodySchema()
        .setRequired(Boolean.FALSE);
    ServerRequest serverRequest = mockServerRequest(null, MediaType.APPLICATION_JSON);

    assertTrue(this.requestBodyHandler.getValues(serverRequest, requestBodyContext, requestBody, new HashMap<>())
        .isEmpty());
  }

  @Test
  void supports_returnsTrue_forJson() {
    assertTrue(this.requestBodyHandler.supports(this.requestBody));
  }

  @Test
  void supports_returnsFalse_forNonJson() {
    this.requestBody.getContent()
        .remove(MediaType.APPLICATION_JSON.toString());
    assertFalse(this.requestBodyHandler.supports(this.requestBody));
  }

  private ServerRequest mockServerRequest(String requestBodyContent, MediaType contentType) {
    ServerRequest serverRequest = Mockito.mock(ServerRequest.class);
    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.header("Content-Type")).thenReturn(Collections.singletonList(contentType.toString()));
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
