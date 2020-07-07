package org.dotwebstack.framework.integrationtest.openapirdf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "dotwebstack.openapi.serializeNull=false")
class SerializeNullTest {

  @Autowired
  private WebTestClient webClient;

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  void openApiRequest_returnsBreweriesWithoutNullValues_whenSerializeNullIsFalse() throws IOException {

    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?expand=postalCode")
        .header("sort", "-postalCode", "name")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    JsonNode expectedObj = mapper.readTree(
        getClass().getResourceAsStream("/results/breweries_sorted_on_postalCode_desc_and_name_asc_without_null.json"));
    JsonNode actualObj = mapper.readTree(result);

    // Assert
    assertEquals(expectedObj, actualObj);
  }
}
