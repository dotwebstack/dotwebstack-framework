package org.dotwebstack.framework.core.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.InternalServerErrorException;
import org.dotwebstack.framework.core.condition.GraphQlNativeDisabled;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Conditional(GraphQlNativeDisabled.class)
@Service
public class ProxyGraphQlService implements GraphQlService {

  private final ObjectMapper objectMapper;

  private final String uri;

  private final HttpClient client;


  public ProxyGraphQlService(@NonNull ObjectMapper proxyObjectMapper, @NonNull String proxyUri) {
    this.objectMapper = proxyObjectMapper;
    this.client = HttpClient.create();
    this.uri = proxyUri;
  }

  @Override
  public ExecutionResult execute(ExecutionInput executionInput) {
    String body = createBody(executionInput);
    ByteBuf byteBuffer = client.post()
        .uri(uri)
        .send(ByteBufMono.fromString(Mono.just(body)))
        .responseSingle((res, content) -> content)
        .block();
    return readBody(byteBuffer);

  }

  @Override
  public CompletableFuture<ExecutionResult> executeAsync(ExecutionInput executionInput) {
    LOG.debug("Executing graphql query using remote proxy with query {}", executionInput.getQuery());
    String body = createBody(executionInput);
    return client.headers(h -> h.set("Content-Type", "application/json"))
        .post()
        .uri(uri)
        .send(ByteBufMono.fromString(Mono.just(body)))
        .responseSingle((res, content) -> content)
        .map(this::readBody)
        .toFuture();
  }

  protected ExecutionResult readBody(ByteBuf byteBuffer) {
    LOG.debug("Reading response");
    try {
      InputStream src = new ByteBufInputStream(byteBuffer);
      ExecutionResult result = objectMapper.readValue(src, ExecutionResult.class);
      return result;
    } catch (IOException e) {
      throw new InternalServerErrorException("Error unmarshalling body from graphQl reponse", e);
    }
  }

  private String createBody(ExecutionInput executionInput) {
    Map<String, Object> body = Map.of("query", executionInput.getQuery(), "operationName", "test");
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Error creating body for graphQl executionInput", e);
    }
  }
}
