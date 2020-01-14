package org.dotwebstack.framework.integrationtest.openapirdf4j;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.containsString;

import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiRdf4jIntegrationTest {

  @Autowired
  private WebTestClient webClient;

  @Test
  public void openApiQuery_200_forProvidedPathParameter() {
    this.webClient.get()
        .uri("/breweries")
        .exchange()
        .expectStatus()
        .isOk();
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
  public void openApiQuery_200_forProvidedHeaderParameter() {
    this.webClient.get()
        .uri("/breweries")
        .header("sort", "name")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void openApiQuery_404_forUnprovidedRequiredParameter() {
    this.webClient.get()
        .uri("/brewery_with_subject")
        .exchange()
        .expectStatus()
        .isBadRequest();
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
  public void openApiQuery_returnsBadRequest_forNonexistentParam() {
    this.webClient.get()
        .uri("/breweries?nonexistent=nonexistent")
        .exchange()
        .expectStatus()
        .isBadRequest();
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

  @Test
  void openApiQuery_ReturnsConfigurationException_ForInvalidPageSize() {
    this.webClient.get()
        .uri("/breweries?pageSize=1")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(String.class)
        .value(containsString("Constraint 'oneOf' [10, 20, 50] violated on 'pageSize' with value '1'"));
  }

  @Test
  void openApiQuery_ReturnsConfigurationException_ForInvalidPage() {
    this.webClient.get()
        .uri("/breweries?page=0")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(String.class)
        .value(containsString("Constraint 'min' [1] violated on 'page' with value '0'"));
  }
}
