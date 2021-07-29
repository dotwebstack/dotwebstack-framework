package org.dotwebstack.framework.integrationtest.openapijson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiJsonIntegrationTest {

  @Autowired
  private WebTestClient webClient;

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void openApiRequest_ReturnsBeers_withDefaultResponse() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/beers")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/beers.json");
  }

  @Test
  void openApiRequest_ReturnsBeer_withDefaultResponse() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/beer/1")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/beer.json");
  }

  @Test
  void openApiRequest_ReturnsBeer_withExpandedBrewery() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/beer/1?expand=brewery")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/beer_brewery.json");
  }

  @Test
  void openApiRequest_ReturnsBeers_withExpandedBrewery() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/beers?expand=brewery")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/beers_brewery.json");
  }

  @Disabled("requires filtering")
  @Test
  void openApiRequest_ReturnsBeers_byCountryAndName_withDefaultResponse() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/beers/Germany/Becks")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/beer_by_country_and_name.json");
  }

  @Test
  void openApiRequest_ReturnsBrewery_withDefaultResponse() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/brewery/1")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/brewery.json");
  }

  @Test
  void openApiRequest_ReturnsBreweries_withDefaultResponse() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries.json");
  }

  @Test
  void openApiRequest_ReturnsProblem_withNotAcceptedContentType() throws IOException {
    // Arrange & Act
    EntityExchangeResult<String> result = webClient.get()
        .uri("/beers")
        .accept(MediaType.APPLICATION_PDF)
        .exchange()
        .expectBody(String.class)
        .returnResult();

    // Assert
    assertThat(result.getStatus(), is(HttpStatus.NOT_ACCEPTABLE));
    assertThat(result.getResponseHeaders()
        .getContentType(), is(MediaType.valueOf("application/problem+json")));
    assertResult(result.getResponseBody(), "/results/beers_not_acceptable.json");
  }

  private void assertResult(String result, String jsonResultPath) throws IOException {
    JsonNode expectedObj = mapper.readTree(getClass().getResourceAsStream(jsonResultPath));
    JsonNode actualObj = mapper.readTree(result);

    // Assert
    assertEquals(expectedObj, actualObj);
  }
}
