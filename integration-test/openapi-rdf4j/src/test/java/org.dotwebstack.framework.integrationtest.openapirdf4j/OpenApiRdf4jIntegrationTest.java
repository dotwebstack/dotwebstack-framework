package org.dotwebstack.framework.integrationtest.openapirdf4j;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.dotwebstack.framework.integrationtest.openapirdf4j.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiRdf4jIntegrationTest {

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
  void openApiRequest_ReturnsBreweries_withStaticFields() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?expand=countries&expand=class&expand=beers.class")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_withStaticFields.json");
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
        .expectHeader()
        .contentType("application/hal+json")
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_name.json");
  }

  @Test
  void openApiRequest_ReturnsBrewery_withSearchNameFromQueryParam() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?searchName=Brouwerij")
        .exchange()
        .expectHeader()
        .contentType("application/hal+json")
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_searchName.json");
  }

  @Test
  void openApiRequest_ReturnsBrewery_withCaseInsensitiveSearchNameFromQueryParam() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?searchName=BROUWERIJ")
        .exchange()
        .expectHeader()
        .contentType("application/hal+json")
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_searchName.json");
  }

  @Test
  void openApiRequest_ReturnsBrewery_withSearchPostalCodeFromQueryParam() throws IOException {
    // Arrange & Act
    String result = webClient.get()
        .uri("/breweries?searchPostalCode=2841&expand=postalCode")
        .exchange()
        .expectHeader()
        .contentType("application/hal+json")
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_searchPostCode.json");
  }

  @ParameterizedTest
  @CsvSource(value = {"*/*:brewery_model_turtle_identifier.json", "text/html:brewery_model_jsonld_filtered.txt",
      "application/n-triples:brewery_model_n-triples_identifier.json"}, delimiter = ':')
  void openApiRequest_ReturnsBreweryModel_withIdentifierFromPathParamAndAcceptHeader(String acceptHeader,
      String expectedResultFile) throws IOException {
    // Arrange & Act
    String actualResult = webClient.get()
        .uri("/brewery/123/model")
        .header("iri", "https://github.com/dotwebstack/beer/id/brewery/123")
        .header("Accept", acceptHeader)
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    String expectedResult = new String(getFileInputStream(expectedResultFile).readAllBytes());
    assertThat(actualResult, equalToIgnoringLineBreaks(expectedResult));
  }

  @ParameterizedTest
  @CsvSource(value = {"application/sparql-results+json:brewery_sparql_result.json",
      "application/sparql-results+xml:brewery_sparql_result.xml"}, delimiter = ':')
  void openApiRequest_ReturnsBrewerySparqlResult_withIdentifierFromPath(String acceptHeader, String expectedResultFile)
      throws IOException {
    // Arrange & Act
    String actualResult = webClient.get()
        .uri("/brewery/123/sparql")
        .header("Accept", acceptHeader)
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    String expectedResult = new String(getFileInputStream(expectedResultFile).readAllBytes());
    assertThat(actualResult.replace("\t", "    "), equalToIgnoringLineBreaks(expectedResult));
  }

  @Test
  void openApiRequest_ReturnsBrewery_withNameFromQueryParamAndAcceptHeaderXml() {
    // Arrange & Act
    webClient.get()
        .uri("/breweries?name=Brouwerij 1923")
        .header("Accept", "application/xml;q=0.5,application/*+json")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType("application/hal+json");
  }

  @Test
  void openApiReques_ReturnsNotAcceptedTest() {
    // Arrange & Act
    webClient.get()
        .uri("/breweries")
        .header("Accept", "application/unsupported")
        .exchange()
        .expectStatus()
        .value(equalTo(406));
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
    assertResult(result, "/results/breweries_filter_name_expand_ingredients.json");
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

  @Test
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
        .uri("/brewery_with_subject?subject=https://github.com/dotwebstack/beer/id/brewery/123&expand=subject")
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
        .header("sort", "-beers.name")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertThat(result, is(CoreMatchers.containsString("Internal server error")));
  }

  @Test
  void openApiRequest_returnsBreweries_forFilterOnAddressSubjectNested() throws IOException {
    // Arrange & Act
    String result = this.webClient.get()
        .uri("/breweries?expand=address&withAddressSubject=https://github.com/dotwebstack/beer/id/address/1")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/breweries_filter_on_addressSubjectNested.json");
  }

  @Test
  void openApiRequest_400_ForInvalidPageSize() {
    this.webClient.get()
        .uri("/breweries?pageSize=3")
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(String.class)
        .value(containsString("Validation of request parameters failed"));
  }

  @Test
  void openApiRequest_400_ForInvalidPage() {
    this.webClient.get()
        .uri("/breweries?page=0")
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(String.class)
        .value(containsString("Validation of request parameters failed"));
  }

  @Test
  void openApiRequest_200_forProvidedPathParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void openApiRequest_200_forProvidedRequiredParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries?expand=beers")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void openApiRequest_200_forProvidedHeaderParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries")
        .header("sort", "name")
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void openApiRequest_404_forUnprovidedRequiredParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/brewery_with_subject")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void openApiRequest_404_forUnknownUri() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/unknown")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void openApiRequest_404_forUnknownUriWithParameter() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/unknown?nonexistent=nonexistent")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void openApiRequest_returnsBadRequest_forNonexistentParam() {
    // Arrange & Act & Assert
    this.webClient.get()
        .uri("/breweries?nonexistent=nonexistent")
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void openApiRequest_404_forUnknownOperation() {
    // Arrange & Act & Assert
    this.webClient.post()
        .uri("/breweries")
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void openApiRequest_returnsOpenApiSpec_forApi() {
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
  void openApiRequest_returnsDefaultXPaginationResponseHeader_whenNoParamsAreProvided() {
    // Arrange & Act
    FluxExchangeResult<String> result = this.webClient.get()
        .uri("/breweries")
        .exchange()
        .expectStatus()
        .isOk()
        .returnResult(String.class);

    // Assert
    HttpHeaders responseHeaders = result.getResponseHeaders();
    assertEquals("1", responseHeaders.get("X-Pagination-Page")
        .get(0));
    assertEquals("10", responseHeaders.get("X-Pagination-Limit")
        .get(0));
  }

  @Test
  void openApiRequest_returnTestAsset() {
    // Arrange & Act
    String result = this.webClient.get()
        .uri("/assets/test.html")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    String expectedResult = "<html>\n" + "<body>Hello World!</body>\n" + "</html>";

    // Assert
    assertEquals(expectedResult, result);
  }

  @Test
  void openApiRequest_returnsXPaginationResponseHeader_whenParamsAreProvided() {
    // Arrange & Act
    FluxExchangeResult<String> result = this.webClient.get()
        .uri("/breweries?page=2&pageSize=2")
        .exchange()
        .expectStatus()
        .isOk()
        .returnResult(String.class);

    // Assert
    HttpHeaders responseHeaders = result.getResponseHeaders();
    assertEquals("2", responseHeaders.get("X-Pagination-Page")
        .get(0));
    assertEquals("2", responseHeaders.get("X-Pagination-Limit")
        .get(0));
  }

  @Test
  void openApiRequest_returnsBrewery_forRequestWithBodyParams() throws IOException {
    // Arrange & Act
    String result = this.webClient.post()
        .uri("/brewery_post?expand=postalCode")
        .body(Mono.just("{\"identifier\": \"123\"}"), String.class)
        .header("Content-Type", MediaType.APPLICATION_JSON.toString())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    // Assert
    assertResult(result, "/results/brewery_postalCode.json");
  }

  private void assertResult(String result, String jsonResultPath) throws IOException {
    JsonNode expectedObj = mapper.readTree(getClass().getResourceAsStream(jsonResultPath));
    JsonNode actualObj = mapper.readTree(result);

    // Assert
    assertEquals(expectedObj, actualObj);
  }

  private InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("results")
        .resolve(filename));
  }
}
