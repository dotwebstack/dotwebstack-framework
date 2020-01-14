package org.dotwebstack.framework.integrationtest.openapirdf4j;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.containsString;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OpenApiRdf4jIntegrationTest {

  @Autowired
  private WebTestClient webClient;

  private ObjectMapper mapper = new ObjectMapper();

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
  void openApiRequest_ReturnsBrewery_withIdentifierFromPathParam() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/brewery/123")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/brewery_identifier.json");
  }

  @Test
  void openApiRequest_ReturnsBrewery_withExpandedPostalCode() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/brewery/123?expand=postalCode")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/brewery_postalCode.json");
  }

  @Test
  void openApiRequest_ReturnsBrewery_withNameFromQueryParam() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?name=Brouwerij 1923")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_name.json");
  }

  @Test
  void openApiRequest_ReturnsBrewery_withNestedExpandedField() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?name=Alfa Brouwerij&expand=beers.ingredients")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_name_expand.json");
  }

  @Test
  void openApiRequest_returnsBreweries_forQueryWithFilterOnDateTimeField() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?foundedAfter=1970-05-29&expand=founded")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_foundedAfter.json");
  }

  @Test
  void openApiRequest_returnsBreweries_forQueryWithMultipleQueryParamFilters() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?foundedAfter=1990-01-01&foundedBefore=2011-01-01&expand=founded")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_foundedAfter_and_foundedBefore.json");
  }

  @Test
  void openApiRequest_returnsBreweries_forSortQueryWithIdentifierAscSorting() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries")
        .header("sort", "identifier")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_sorted_on_identifier_asc.json");
  }

  @Test
  void openApiRequest_returnsError_forSortQueryWithNonExistentSortingValue() {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries")
        .header("sort", "nonexistent")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertTrue(result.contains("Parameter 'sort' has (an) invalid value(s)"));
  }

  @Test()
  void openApiRequest_returnsBreweries_forSortQueryWithMultipleSorting() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?expand=postalCode")
        .header("sort", "-postalCode", "name")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_sorted_on_postalCode_desc_and_name_asc.json");
  }

  @Test
  public void openApiRequest_200_forProvidedPathParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void openApiRequest_200_forProvidedRequiredParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries?expand=beers")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void openApiRequest_200_forProvidedHeaderParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries")
        .header("sort", "name")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void openApiRequest_404_forUnprovidedRequiredParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/brewery_with_subject")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  public void openApiRequest_404_forUnknownUri() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/unknown")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void openApiRequest_404_forUnknownUriWithParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/unknown?nonexistent=nonexistent")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void openApiRequest_returnsBadRequest_forNonexistentParam() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries?nonexistent=nonexistent")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }
  
  @Test
  public void openApiRequest_404_forUnknownOperation() {
    // Arrange & Act & Assert
    this.webClient.post()
        .uri("/breweries")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  public void openApiRequest_returnsOpenApiSpec_forApi() {
    // Arrange & Act
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

    // Assert
    assertTrue(openApiSpec.contains("/breweries:"));
    assertFalse(openApiSpec.contains("x-dws-expr"));
    assertFalse(openApiSpec.contains("x-dws-envelope: true"));
  }

  @Test
  void openApiRequest_ReturnsBreweries_WithTransformedAggregate() throws IOException {
    // Arrange & Act
    String result = this.webClient.get()
        .uri("/breweries?expand=hasBeers")
        .header("sort", "name")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_with_transformaggregate.json");
  }

  @Test
  void openApiRequest_returnsBrewery_forFilteredOnSubjectIriFromQueryParam() throws IOException {
    // Arrange & Act
    String result = this.webClient.get()
        .uri("/brewery_with_subject?subject=https://github.com/dotwebstack/beer/id/brewery/123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/brewery_filtered_by_subject.json");
  }

  @Test
  void openApiRequest_throwsException_SortedOnListBeerSubjectDesc() throws IOException {
    // Arrange & Act
    String result = this.webClient.get()
        .uri("/breweries?expand=beers")
        .header("sort", "-beers.subject")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertTrue(result.contains("which contains a list"));
  }

  @Test
  void openApiRequest_returnsBreweries_forFilterOnAddressSubjectNested() throws IOException {
    // Arrange & Act
    String result = this.webClient.get()
        .uri("/breweries?withAddressSubject=https://github.com/dotwebstack/beer/id/address/1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_on_addressSubjectNested.json");
  }

  private void assertResult(String result, String jsonResultPath) throws IOException {
    JsonNode expectedObj = mapper.readTree(getClass().getResourceAsStream(jsonResultPath));
    JsonNode actualObj = mapper.readTree(result);

    // Assert
    assertEquals(expectedObj, actualObj);
  }

  @Test
  void openApiRequest_ReturnsConfigurationException_ForInvalidPageSize() {
    this.webClient.get()
        .uri("/breweries?pageSize=3")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(String.class)
        .value(containsString("Constraint 'oneOf' [1, 2, 5, 10] violated on 'pageSize' with value '3'"));
  }

  @Test
  void openApiRequest_ReturnsConfigurationException_ForInvalidPage() {
    this.webClient.get()
        .uri("/breweries?page=0")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(String.class)
        .value(containsString("Constraint 'min' [1] violated on 'page' with value '0'"));
  }
}
