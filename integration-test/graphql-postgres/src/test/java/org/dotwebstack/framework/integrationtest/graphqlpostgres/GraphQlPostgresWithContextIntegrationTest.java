package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.jooq.tools.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("with-context")
@AutoConfigureWebTestClient
@Testcontainers
class GraphQlPostgresWithContextIntegrationTest {

  private static final String ERRORS = "errors";

  @Autowired
  private WebTestClient client;

  @Autowired
  private GraphQL graphQL;

  private final ObjectMapper mapper = new ObjectMapper();

  @Container
  static GraphQlPostgresWithContextIntegrationTest.TestPostgreSqlContainer postgreSqlContainer =
      new GraphQlPostgresWithContextIntegrationTest.TestPostgreSqlContainer()
          .withClasspathResourceMapping("config/model-context", "/docker-entrypoint-initdb.d", BindMode.READ_ONLY);

  private static class TestPostgreSqlContainer extends PostgreSQLContainer<TestPostgreSqlContainer> {
    public TestPostgreSqlContainer() {
      super(DockerImageName.parse("postgis/postgis:11-3.1")
          .asCompatibleSubstituteFor("postgres"));
    }
  }

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("dotwebstack.postgres.host", () -> postgreSqlContainer.getHost());
    registry.add("dotwebstack.postgres.port", () -> postgreSqlContainer.getFirstMappedPort());
    registry.add("dotwebstack.postgres.username", () -> postgreSqlContainer.getUsername());
    registry.add("dotwebstack.postgres.password", () -> postgreSqlContainer.getPassword());
    registry.add("dotwebstack.postgres.database", () -> postgreSqlContainer.getDatabaseName());
  }

  @Test
  void getRequest_returnBeers_forActualDefault() {
    var query = "{\n" + "  beers(context:{}) {\n" + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n"
        + "        brewery {\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    JsonNode json = executeQuery(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"),
        hasItems(
            hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z")),
            hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("name"), equalTo("Beer 3")),
            hasEntry(equalTo("name"), equalTo("Beer 4")), hasEntry(equalTo("name"), equalTo("Beer 5")),
            hasEntry(equalTo("name"), equalTo("Beer 6"))))));
  }

  @Test
  void getRequest_returnBeers_forActual() {
    var query = "{\n" + "  beers(context:{validOn: \"2020-09-01\", availableOn: \"2020-02-01T00:00:00Z\"}) {\n"
        + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n" + "        brewery {\n"
        + "          identifier_brewery\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    JsonNode json = executeQuery(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"),
        hasItems(
            hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z")),
            hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("name"), equalTo("Beer 3")),
            hasEntry(equalTo("name"), equalTo("Beer 4")), hasEntry(equalTo("name"), equalTo("Beer 5")),
            hasEntry(equalTo("name"), equalTo("Beer 6"))))));
  }

  @Test
  void getRequest_returnBeer_forActual() {
    var query =
        "{\n" + "  beer(identifier_beer: \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\", context:{validOn: \"2020-09-01\", "
            + "availableOn: \"2020-02-01T00:00:00Z\"}) {\n" + "    name\n" + "    soldPerYear\n" + "    brewery {\n"
            + "      identifier_brewery\n" + "      name\n" + "    }\n" + "  }\n" + "}";

    JsonNode json = executeQuery(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo("beer"),
        hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z"))));
  }

  @Test
  void getRequest_returnBeers_forValidFirstAvailableActual() {
    var query = "{\n" + "  beers(context:{validOn: \"2018-06-01\", availableOn: \"2020-02-01T00:00:00Z\"}) {\n"
        + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n" + "        brewery {\n"
        + "          identifier_brewery\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    JsonNode json = executeQuery(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"),
        hasItems(
            hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2018-01-01, availableStart: 2019-04-01T12:00:00Z")),
            hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("name"), equalTo("Beer 3")),
            hasEntry(equalTo("name"), equalTo("Beer 4")), hasEntry(equalTo("name"), equalTo("Beer 5")),
            hasEntry(equalTo("name"), equalTo("Beer 6"))))));
  }

  @Test
  void getRequest_returnBeers_forValidActualAvailableFirst() {
    var query = "{\n" + "  beers(context:{validOn: \"2018-06-01\", availableOn: \"2019-03-01T00:00:00Z\"}) {\n"
        + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n" + "        brewery {\n"
        + "          identifier_brewery\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    JsonNode json = executeQuery(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"),
        hasItems(
            hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2018-01-01, availableStart: 2018-02-01T12:00:00Z")),
            hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("name"), equalTo("Beer 3")),
            hasEntry(equalTo("name"), equalTo("Beer 4")), hasEntry(equalTo("name"), equalTo("Beer 5")),
            hasEntry(equalTo("name"), equalTo("Beer 6"))))));
  }

  @Test
  void getRequest_returnsEmptyResult_forQueryBeforeOrigin() {
    var query = "{\n" + "  beers(context:{validOn: \"2015-06-01\", availableOn: \"2015-02-01T00:00:00Z\"}) {\n"
        + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n" + "        brewery {\n"
        + "          identifier_brewery\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    JsonNode json = executeQuery(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"), iterableWithSize(0))));
  }

  private JsonNode executeQuery(String query) {
    return executeGetRequest(query);
  }

  private JsonNode executeGetRequest(String query) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/");

    if (!StringUtils.isBlank(query)) {
      uriBuilder.queryParam("query", query);
    }

    var result = client.get()
        .uri(uriBuilder.build()
            .toUri())
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    return getJson(result);
  }

  private JsonNode getJson(String result) {
    try {
      return mapper.readTree(result);
    } catch (JsonProcessingException exception) {
      throw ExceptionHelper.illegalArgumentException(String.format("Failed to parse string to json: %s", result));
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getDataFromJsonNode(JsonNode json) {
    try {
      return mapper.readValue(json.get("data")
          .toString(), Map.class);
    } catch (JsonProcessingException exception) {
      throw ExceptionHelper.illegalArgumentException(String.format("Failed to parse Json to Map: %s", json));
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void assertThat(Object object, Matcher<?> matcher) {
    MatcherAssert.assertThat(object, (Matcher) matcher);
  }
}
