package org.dotwebstack.framework.core.graphql.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;


@ExtendWith(MockitoExtension.class)
class GraphQlProxyServiceTest {

  @Mock
  private HttpClient httpClient;

  private GraphQlProxyService proxyService;

  @BeforeEach
  void init() {
    ObjectMapper objectMapper = new ObjectMapper();
    SimpleModule sm = new SimpleModule("Graphql");
    sm.addDeserializer(ExecutionResult.class, new ExecutionResultDeserializer(ExecutionResult.class));
    objectMapper.registerModule(sm);

    proxyService = new GraphQlProxyService(objectMapper, "http://localhost:8081/", httpClient);
  }

  @Test
  void execute_returnsResult_success() {
    // Arrange
    ExecutionInput input = ExecutionInput.newExecutionInput()
        .query("myquery")
        .build();
    mockResult("{\"data\":{\"key\":\"value\"}}");

    // Act
    ExecutionResult result = proxyService.execute(input);

    // Assert
    assertThat(result.getData(), is(Map.of("key", "value")));
  }

  @Test
  void execute_throwsException_forInvalidResponse() {
    // Arrange
    ExecutionInput input = ExecutionInput.newExecutionInput()
        .query("myquery")
        .build();
    mockResult("{\"data\":[_]}");

    // Act / Assert
    assertThrows(GraphQlProxyException.class, () -> proxyService.execute(input));
  }

  @Test
  void execute_throwsException_forHttpErrorCode() {
    // Arrange
    ExecutionInput input = ExecutionInput.newExecutionInput()
        .query("myquery")
        .build();
    mockResult("{\"data\":[_]}");

    // Act / Assert
    assertThrows(GraphQlProxyException.class, () -> proxyService.execute(input));
  }


  @Test
  void executeAsync_returnsResult_success() throws ExecutionException, InterruptedException {
    // Arrange
    ExecutionInput input = ExecutionInput.newExecutionInput()
        .query("myquery")
        .build();
    mockResult("{\"data\":{\"key\":\"value\"}}");

    // Act
    CompletableFuture<ExecutionResult> cf = proxyService.executeAsync(input);

    // Assert
    assertThat(cf.get()
        .getData(), is(Map.of("key", "value")));
  }

  @Test
  void checkResult_doesntThrowException_onNon200() {
    // Arrange
    HttpClientResponse response = mock(HttpClientResponse.class);
    when(response.status()).thenReturn(HttpResponseStatus.OK);

    // Act / Assert
    assertDoesNotThrow(() -> proxyService.checkResult()
        .apply(response, mock(ByteBufMono.class)));
  }

  @Test
  void checkResult_throwsException_onNon200Code() {
    // Arrange
    HttpClientResponse response = mock(HttpClientResponse.class);
    when(response.status()).thenReturn(HttpResponseStatus.INSUFFICIENT_STORAGE);

    // Act / Assert
    assertThrows(GraphQlProxyException.class, () -> proxyService.checkResult()
        .apply(response, mock(ByteBufMono.class)));
  }

  @SuppressWarnings("unchecked")
  private void mockResult(String responseJson) {
    ByteBuf response = UnpooledByteBufAllocator.DEFAULT.buffer();
    response.writeBytes(responseJson.getBytes());

    HttpClient.RequestSender sender = mock(HttpClient.RequestSender.class);
    // mocking final method 'headers' requires file
    // src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
    doReturn(httpClient).when(httpClient)
        .headers(any(Consumer.class));
    doReturn(sender).when(httpClient)
        .post();
    doReturn(sender).when(sender)
        .uri(any(String.class));

    HttpClient.ResponseReceiver<?> receiver = mock(HttpClient.ResponseReceiver.class);
    doReturn(receiver).when(sender)
        .send(any(Publisher.class));
    doReturn(Mono.just(response)).when(receiver)
        .responseSingle(any());
  }

}
