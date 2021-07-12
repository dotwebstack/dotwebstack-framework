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
import org.dotwebstack.framework.core.InternalServerErrorException;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClient;

@ConditionalOnMissingBean(NativeGraphQlService.class)
@Service
public class ProxyGraphQlService implements GraphQlService {

  private final ObjectMapper objectMapper;

  private final HttpClient client;

  public ProxyGraphQlService(@NonNull DotWebStackConfiguration config) {
    objectMapper = new ObjectMapper();
    client = HttpClient.create();

    String proxy = config.getSettings()
        .getGraphql()
        .getProxy();
    client.baseUrl(proxy);
  }

  @Override
  public ExecutionResult execute(ExecutionInput executionInput) {
    String query = executionInput.getQuery();
    String body = createBody(query);
    ByteBuf byteBuffer = client.post()
        .send(ByteBufMono.fromString(Mono.just(body)))
        .responseSingle((res, content) -> content)
        .block();
    return readBody(byteBuffer);

  }

  @Override
  public CompletableFuture<ExecutionResult> executeAsync(ExecutionInput executionInput) {
    String query = executionInput.getQuery();
    String body = createBody(query);
    return client.post()
        .send(ByteBufMono.fromString(Mono.just(body)))
        .responseSingle((res, content) -> content)
        .map(this::readBody)
        .toFuture();
  }

  protected ExecutionResult readBody(ByteBuf byteBuffer) {
    try {
      return objectMapper.readValue((InputStream) new ByteBufInputStream(byteBuffer), ExecutionResult.class);
    } catch (IOException e) {
      throw new InternalServerErrorException("Error unmarshalling body from graphQl reponse", e);
    }
  }

  private String createBody(String query) {
    Map<String, Object> body = Map.of("query", query, "operationName", "what?");
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Error creating body for graphQl query", e);
    }
  }
}
