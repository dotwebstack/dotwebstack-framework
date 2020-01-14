package org.dotwebstack.framework.integrationtest.openapirdf4j;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import graphql.ExecutionResult;
import io.swagger.v3.core.util.Json;
import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiRdf4jIntegrationTest {

  @Autowired
  private WebTestClient webClient;

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  void graphqlQuery_ReturnsMap_ForObjectQueryField() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/brewery/123")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    JsonNode expectedObj = mapper.readTree(getClass().getResourceAsStream("/results/brewery_123.json"));
    JsonNode actualObj = mapper.readTree(result);

    // Assert
    assertEquals(expectedObj, actualObj);
  }

  @Test
  void graphqlQuery_ReturnsMap_ForNestedListNonNullQuery() throws IOException {
    //String query = "{ beer(identifier: \"6\") { identifier, beerTypesRaw { name }  }}";

    // Arrange & Act
    String result = webClient.get()
        .uri("/beers/6?expand=beerTypesRaw")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    JsonNode expectedObj = mapper.readTree(getClass().getResourceAsStream("/results/beer_6.json"));
    JsonNode actualObj = mapper.readTree(result);

    // Assert
    assertEquals(expectedObj, actualObj);
  }

  @Test
  public void openApiQuery_404_forUnknownUri() {
    this.webClient.get()
        .uri("/unknown")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void openApiQuery_404_forUnknownUriWithParameter() {
    this.webClient.get()
        .uri("/unknown?nonexistent=nonexistent")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void openApiQuery_200_forProvidedRequiredParameter() {
    this.webClient.get()
        .uri("/breweries?expand=beers")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void openApiQuery_returnsBadRequest_forNonexistentParam() {
    this.webClient.get()
        .uri("/breweries?nonexistent=nonexistent")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  public void openApiQuery_200_forProvidedHeaderParameter() {
    this.webClient.get()
        .uri("/breweries")
        .header("sort", "name")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void openApiQuery_404_forUnknownOperation() {
    this.webClient.post()
        .uri("/breweries")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void openApiQuery_returnsOpenApiSpec_forApi() {

    FluxExchangeResult<String> result = this.webClient.get()
        .uri("/")
        .exchange()
        .expectStatus()
        .isOk()
        .returnResult(String.class);

    StringBuilder builder = new StringBuilder();
    result.getResponseBody()
        .toStream()
        .forEach(builder::append);
    String openApiSpec = builder.toString();

    assertTrue(openApiSpec.contains("/breweries:"));
    assertFalse(openApiSpec.contains("x-dws-expr"));
    assertFalse(openApiSpec.contains("x-dws-envelope: true"));
  }

}
