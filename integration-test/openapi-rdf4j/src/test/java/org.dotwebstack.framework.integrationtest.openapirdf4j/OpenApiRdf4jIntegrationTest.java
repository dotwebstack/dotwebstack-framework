package org.dotwebstack.framework.integrationtest.openapirdf4j;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
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
  void graphqlQuery_ReturnsMap_ForObjectQueryField() throws IOException {
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
  void graphqlQuery_ReturnsMap_ForNestedListNonNullQuery() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/beers/6?expand=beerTypesRaw")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/beer_identifier.json");
  }

  @Test
  void graphqlQuery_ReturnsMap_ForObjectQueryNestedField() throws IOException {
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
  void graphqlQuery_ReturnsMap_ForQueryWithFilter() throws IOException {
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
  void graphqlQuery_ReturnsResult_forQueryWithNesting() throws IOException {
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
  void graphqlQuery_ReturnsMap_ForQueryWithFilterOnDateTimeField() throws IOException {
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
  void graphqlQuery_ReturnsMap_ForQueryWithTwoFiltersOnDateTimeField() throws IOException {
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
  void graphqlQuery_ReturnsMap_ForSortQueryWithDefaultSorting() throws IOException {
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
  void graphqlQuery_ReturnsMap_ForSortQueryWithIdentifierAscSorting() throws IOException {
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
  void graphqlQuery_ReturnsMap_ForSortQueryWithInvalidSortingValue() {
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
  void graphqlQuery_ReturnsMap_ForSortQueryWithNameMultipleSorting() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?expand=postalCode")
        .header("sort", new String[] {"-postalCode", "name"})
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_sorted_on_postalCode_desc_and_name_asc.json");
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

  @Test
  void graphQlQuery_ReturnesBreweries_FilteredByBeerCount2() throws IOException {
    // Arrange / Act
    String result = this.webClient.get()
        .uri("/breweries?beerCount=2")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filtered_by_beercount2.json");
  }

  @Test
  void graphQlQuery_ReturnsBreweries_FilteredByBeerCount0() throws IOException {
    // Arrange / Act
    String result = this.webClient.get()
        .uri("/breweries?beerCount=0")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filtered_by_beercount0.json");
  }

  @Test
  void graphQlQuery_ReturnsBreweries_FilteredByBeerCount2MissingEdge() throws IOException {
    // Arrange / Act
    String result = this.webClient.get()
        .uri("/breweries?beerCount=2")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filtered_by_beercount_missingedge.json");
  }

  @Test
  void graphQlQuery_ReturnsBreweries_WithTransformedAggregate() throws IOException {
    // Arrange / Act
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
  void graphqlQuery_ReturnsBrewery_FilteredBySubject() throws IOException {
    // Arrange / Act
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
  void graphQlQuery_ReturnsBreweries_SortedOnSubjectAsc() throws IOException {
    // Arrange / Act
    String result = this.webClient.get()
        .uri("/breweries")
        .header("sort", "subject")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_sorted_on_subject_asc.json");
  }

  @Test
  void graphQlQuery_ReturnsBreweries_SortedOnSubjectDesc() throws IOException {
    // Arrange / Act
    String result = this.webClient.get()
        .uri("/breweries")
        .header("sort", "-subject")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_sorted_on_subject_desc.json");
  }

  @Test
  void graphQlQuery_throwsException_SortedOnListBeerSubjectDesc() throws IOException {
    // Arrange / Act
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
  void graphQlQuery_returnsBreweries_SortedOnAddressSubjectDesc() throws IOException {
    // Arrange / Act
    String result = this.webClient.get()
        .uri("/breweries?expand=address")
        .header("sort", "-address.subject")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_sorted_on_addresssubject_desc.json");
  }

  @Test
  void graphQlQuery_returnsBreweries_FilterOnAddressSubjectNested() throws IOException {
    // Arrange / Act
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

}
