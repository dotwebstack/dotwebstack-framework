package org.dotwebstack.framework.ext.orchestrate.config;

import static graphql.schema.FieldCoordinates.coordinates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.dotwebstack.framework.ext.orchestrate.config.OrchestrateConfigurationProperties.SubschemaProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

class OrchestrateConfigurationTest {

  static final String ROOT_KEY = "dbeerpedia";

  private final static MockWebServer mockWebServer = new MockWebServer();

  private final OrchestrateConfigurationProperties configurationProperties = new OrchestrateConfigurationProperties();

  private final WebClient.Builder webclientBuilder = WebClient.builder()
      .clientConnector(new ReactorClientHttpConnector())
      .codecs(configurer -> configurer.defaultCodecs()
          .jackson2JsonEncoder(new Jackson2JsonEncoder(new ObjectMapper(), MediaType.APPLICATION_JSON)));

  @BeforeAll
  static void beforeAll() throws IOException {
    mockWebServer.start();
  }

  @AfterAll
  static void afterAll() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void schema_wrapsUnmodifiedSchema_ifNoModifiersPresent() {
    configurationProperties.setRoot(ROOT_KEY);
    configurationProperties.setSubschemas(Map.of(ROOT_KEY, createSubschema()));
    var orchestrateConfiguration = new OrchestrateConfiguration(configurationProperties, webclientBuilder, List.of());

    mockWebServer.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(getIntrospectionResponseBody()));

    var schema = orchestrateConfiguration.schema();

    assertThat(schema, notNullValue());
    assertThat(schema.getObjectType("Brewery"), notNullValue());

    var queryType = schema.getQueryType();
    var codeRegistry = schema.getCodeRegistry();
    assertThat(codeRegistry.hasDataFetcher(coordinates(queryType, queryType.getFieldDefinition("brewery"))), is(true));
  }

  @Test
  void schema_throwsException_ifIntrospectionFails() {
    configurationProperties.setRoot(ROOT_KEY);
    configurationProperties.setSubschemas(Map.of(ROOT_KEY, createSubschema()));
    var orchestrateConfiguration = new OrchestrateConfiguration(configurationProperties, webclientBuilder, List.of());

    mockWebServer.enqueue(new MockResponse().setResponseCode(INTERNAL_SERVER_ERROR.value()));

    assertThrows(ExecutionException.class, orchestrateConfiguration::schema);
  }

  private SubschemaProperties createSubschema() {
    var subschemaProperties = new SubschemaProperties();
    subschemaProperties.setEndpoint(URI.create(String.format("http://%s:%d", mockWebServer.getHostName(), mockWebServer.getPort())));
    return subschemaProperties;
  }

  @SneakyThrows
  private static Buffer getIntrospectionResponseBody() {
    var resource = new ClassPathResource("introspection.json");
    return new Buffer().readFrom(resource.getInputStream());
  }
}