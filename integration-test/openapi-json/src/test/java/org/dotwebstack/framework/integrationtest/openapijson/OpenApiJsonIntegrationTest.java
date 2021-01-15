package org.dotwebstack.framework.integrationtest.openapijson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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

  @Test
  @Disabled("Multiple arguments not supported yet!")
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

  private void assertResult(String result, String jsonResultPath) throws IOException {
    JsonNode expectedObj = mapper.readTree(getClass().getResourceAsStream(jsonResultPath));
    JsonNode actualObj = mapper.readTree(result);

    // Assert
    assertEquals(expectedObj, actualObj);
  }
}
