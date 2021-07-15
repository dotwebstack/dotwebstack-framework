package org.dotwebstack.framework.core.graphql.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.condition.GraphQlNativeDisabled;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientResponse;

@Slf4j
@Conditional(GraphQlNativeDisabled.class)
@Service
public class GraphQlProxyService implements GraphQlService {

  private final ObjectMapper objectMapper;

  private final String uri;

  private final HttpClient client;

  public GraphQlProxyService(@NonNull ObjectMapper proxyObjectMapper, @NonNull String proxyUri,
      @NonNull HttpClient proxyHttpClient) {
    this.objectMapper = proxyObjectMapper;
    this.uri = proxyUri;
    this.client = proxyHttpClient;
  }

  @Override
  public ExecutionResult execute(@NonNull ExecutionInput executionInput) {
    LOG.debug("Executing graphql query using remote proxy with query {}", executionInput.getQuery());
    String body = createBody(executionInput);
    ByteBuf byteBuffer = executePost(body).block();
    return readBody(byteBuffer);
  }

  @Override
  public CompletableFuture<ExecutionResult> executeAsync(@NonNull ExecutionInput executionInput) {
    LOG.debug("Executing graphql query using remote proxy with query {}", executionInput.getQuery());
    String body = createBody(executionInput);
    return executePost(body).map(this::readBody)
        .toFuture();
  }

  protected Mono<ByteBuf> executePost(String body) {
    return client.headers(h -> h.set("Content-Type", "application/json"))
        .post()
        .uri(uri)
        .send(ByteBufMono.fromString(Mono.just(body)))
        .responseSingle(checkResult());
  }

  private ExecutionResult readBody(ByteBuf byteBuffer) {
    try (InputStream src = new ByteBufInputStream(byteBuffer)) {
      return objectMapper.readValue(src, ExecutionResult.class);
    } catch (IOException e) {
      throw new GraphQlProxyException("Error unmarshalling body from graphQl reponse", e);
    }
  }

  protected String createBody(ExecutionInput executionInput) {
    Map<String, Object> body = Map.of("query", executionInput.getQuery(), "operationName", "test");
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new GraphQlProxyException("Error creating body for graphQl executionInput", e);
    }
  }

  protected BiFunction<HttpClientResponse, ByteBufMono, Mono<ByteBuf>> checkResult() {
    return (res, content) -> {
      if (res.status() == HttpResponseStatus.OK) {
        return content;
      } else {
        throw new GraphQlProxyException("Graphql Proxy returned status code {}", res.status()
            .code());
      }
    };
  }
}
