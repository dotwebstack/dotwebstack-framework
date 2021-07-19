package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.test.TestApplication;
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
@ActiveProfiles("with-paging")
@AutoConfigureWebTestClient
@Testcontainers
class GraphQlPostgresWithPagingIntegrationTest {

  private static final String ERRORS = "errors";

  @Autowired
  private WebTestClient client;

  @Autowired
  private GraphQL graphQL;

  private final ObjectMapper mapper = new ObjectMapper();

  @Container
  static GraphQlPostgresWithPagingIntegrationTest.TestPostgreSqlContainer postgreSqlContainer =
      new GraphQlPostgresWithPagingIntegrationTest.TestPostgreSqlContainer()
          .withClasspathResourceMapping("config/model", "/docker-entrypoint-initdb.d", BindMode.READ_ONLY);

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
  void getRequest_ReturnsBreweries_withPagingArguments() {
    var query = "{\n" + "    breweries(first: 2, offset: 1) {\n" + "      nodes {\n" + "        identifier_brewery\n"
        + "        name\n" + "      }\n" + "    ,offset\n" + "    }\n" + "}";

    JsonNode json = executeGetRequestDefault(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data,
        equalTo(Map.of("breweries",
            Map.of("nodes",
                List.of(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X"),
                    Map.of("identifier_brewery", "6e8f89da-9676-4cb9-801b-aeb6e2a59ac9", "name", "Brewery Y")),
                "offset", 1))));
  }

  @Test
  void getRequest_ReturnsBreweries_withPagingAndFilterArguments() {
    var query =
        "{\n" + "    breweries(first: 2, offset: 1, filter: {name: {not: {eq: \"Brewery Y\"}}}) {\n" + "      nodes {\n"
            + "        identifier_brewery\n" + "        name\n" + "      }\n" + "    ,offset\n" + "    }\n" + "}";

    JsonNode json = executeGetRequestDefault(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data,
        equalTo(Map.of("breweries",
            Map.of("nodes",
                List.of(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X"),
                    Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c31959", "name", "Brewery Z")),
                "offset", 1))));
  }

  @Test
  void getRequest_ReturnsBreweriesWithBeers_withPagingArguments() {
    var query = "{\n" + "  breweries(first: 2, offset: 0) {\n" + "    nodes {\n" + "      identifier_brewery\n"
        + "      name\n" + "      beers {\n" + "        nodes {\n" + "          identifier_beer\n" + "          name\n"
        + "        }\n" + "      }\n" + "    }\n" + "    , offset\n" + "  }\n" + "}";

    JsonNode json = executeGetRequestDefault(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data,
        equalTo(
            Map.of("breweries",
                Map.of("nodes",
                    List.of(
                        Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "name", "Brewery S",
                            "beers",
                            Map.of("nodes",
                                List.of(Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name",
                                    "Beer 6")))),
                        Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X",
                            "beers",
                            Map.of("nodes", List.of(
                                Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
                                Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"),
                                Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4"))))),
                    "offset", 0))));
  }

  @Test
  void getRequest_ReturnsBeersWithBrewery_withoutPagingArguments() {
    var query = "{\n" + "  beers {\n" + "    nodes {\n" + "      identifier_beer\n" + "      name\n"
        + "      brewery {\n" + "        identifier_brewery\n" + "        name\n" + "      }\n" + "    }\n"
        + "    , offset\n" + "  }\n" + "}";

    JsonNode json = executeGetRequestDefault(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data,
        equalTo(Map.of("beers",
            Map.of("nodes",
                List.of(
                    Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1", "brewery",
                        Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")),
                    Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2", "brewery",
                        Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")),
                    Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3", "brewery",
                        Map.of("identifier_brewery", "6e8f89da-9676-4cb9-801b-aeb6e2a59ac9", "name", "Brewery Y")),
                    Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4", "brewery",
                        Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")),
                    Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5", "brewery",
                        Map.of("identifier_brewery", "6e8f89da-9676-4cb9-801b-aeb6e2a59ac9", "name", "Brewery Y")),
                    Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6", "brewery",
                        Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "name", "Brewery S"))),
                "offset", 0))));
  }

  @Test
  void getRequest_returnsBeersWithIngredients_forQueryWithJoinTable() {
    String query = "{\n" + "  beers(first: 10, offset: 0) {\n" + "    nodes {\n" + "      identifier_beer\n"
        + "      name\n" + "      ingredients{\n" + "        nodes {\n" + "          identifier_ingredient\n"
        + "          name\n" + "        }\n" + "      }\n" + "    }\n" + "  }\n" + "}";

    JsonNode json = executeGetRequestDefault(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data,
        equalTo(Map.of("beers",
            Map.of("nodes",
                List.of(
                    Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1", "ingredients",
                        Map.of("nodes", List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"),
                            Map.of("identifier_ingredient", "cd79538a-5fbb-11eb-ae93-0242ac130002", "name", "Orange"),
                            Map.of("identifier_ingredient", "cd79545c-5fbb-11eb-ae93-0242ac130002", "name",
                                "Caramel")))),
                    Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2", "ingredients",
                        Map.of("nodes", List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"),
                            Map.of("identifier_ingredient", "cd79538a-5fbb-11eb-ae93-0242ac130002", "name",
                                "Orange")))),
                    Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3", "ingredients",
                        Map.of("nodes", List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"),
                            Map.of("identifier_ingredient", "cd79545c-5fbb-11eb-ae93-0242ac130002", "name",
                                "Caramel")))),
                    Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4", "ingredients",
                        Map.of("nodes", List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast")))),
                    Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5", "ingredients",
                        Map.of("nodes", List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast")))),
                    Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6", "ingredients",
                        Map.of("nodes", List.of(Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002",
                            "name", "Water")))))))));
  }

  private JsonNode executeGetRequestDefault(String query) {
    return executeGetRequest(query, "", "");
  }

  private JsonNode executeGetRequest(String query, String operationName, String variables) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/");

    if (!StringUtils.isBlank(query)) {
      uriBuilder.queryParam("query", query);
    }

    if (!StringUtils.isBlank(operationName)) {
      uriBuilder.queryParam("operationName", operationName);
    }

    if (!StringUtils.isBlank(variables)) {
      uriBuilder.queryParam("variables", variables);
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
}
