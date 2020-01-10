package org.dotwebstack.framework.integrationtest.openapirdf4j;

import org.dotwebstack.framework.test.TestApplication;
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
public class OpenApiRdf4jIntegrationTest {

  @Autowired
  private WebTestClient webClient;

  @Test
  public void openApiQuery_404_forUnknownUri() {
    this.webClient.get()
        .uri("/unknown")
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
}
