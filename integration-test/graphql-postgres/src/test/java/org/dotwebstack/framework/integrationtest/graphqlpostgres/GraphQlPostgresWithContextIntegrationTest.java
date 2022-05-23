package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.dotwebstack.framework.integrationtest.graphqlpostgres.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import graphql.GraphQL;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
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

  @Autowired
  private WebTestClient client;

  @Autowired
  private GraphQL graphQL;

  @Container
  static final GraphQlPostgresWithContextIntegrationTest.TestPostgreSqlContainer postgreSqlContainer =
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
    registry.add("dotwebstack.postgres.host", postgreSqlContainer::getHost);
    registry.add("dotwebstack.postgres.port", postgreSqlContainer::getFirstMappedPort);
    registry.add("dotwebstack.postgres.username", postgreSqlContainer::getUsername);
    registry.add("dotwebstack.postgres.password", postgreSqlContainer::getPassword);
    registry.add("dotwebstack.postgres.database", postgreSqlContainer::getDatabaseName);
  }

  @Test
  void getRequest_returnBeer_forActual() {
    var query =
        "{\n" + "  beer(identifier_beer: \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\", context:{validOn: \"2020-09-01\", "
            + "availableOn: \"2020-02-01T00:00:00Z\"}) {\n" + "    name\n" + "    soldPerYear\n" + "    brewery {\n"
            + "      identifier_brewery\n" + "      name\n" + "    }\n" + "  }\n" + "}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo("beer"),
        hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z"))));
  }

  @Test
  void getRequest_returnBeer_NullIfNotExist() {
    var query = "{\n" + "  beer(identifier_beer: \"11111\", context:{validOn: \"2020-09-01\", "
        + "availableOn: \"2020-02-01T00:00:00Z\"}) {\n" + "    name\n" + "    soldPerYear\n" + "    brewery {\n"
        + "      identifier_brewery\n" + "      name\n" + "    }\n" + "  }\n" + "}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data, hasEntry(equalTo("beer"), nullValue()));
  }

  @Test
  void getRequest_returnBeers_forDefault() {
    var query = "{\n" + "  beers(context:{}) {\n" + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n"
        + "        brewery {\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"),
        hasItems(
            hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z")),
            hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("name"), equalTo("Beer 3")),
            hasEntry(equalTo("name"), equalTo("Beer 4")), hasEntry(equalTo("name"), equalTo("Beer 5")),
            hasEntry(equalTo("name"), equalTo("Beer 6"))))));
  }

  @Test
  void getRequest_returnBeers_forDefault_ifContextNull() {
    var query = "{\n" + "  beers {\n" + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n"
        + "        brewery {\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"),
        hasItems(
            hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z")),
            hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("name"), equalTo("Beer 3")),
            hasEntry(equalTo("name"), equalTo("Beer 4")), hasEntry(equalTo("name"), equalTo("Beer 5")),
            hasEntry(equalTo("name"), equalTo("Beer 6"))))));
  }

  @Test
  void getRequest_returnsEmptyResult_forQueryBeforeOrigin() {
    var query = "{\n" + "  beers(context:{validOn: \"2015-06-01\", availableOn: \"2015-02-01T00:00:00Z\"}) {\n"
        + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n" + "        brewery {\n"
        + "          identifier_brewery\n" + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo("beers"), hasEntry(equalTo("nodes"), iterableWithSize(0))));
  }

  @ParameterizedTest
  @CsvSource(
      value = {"2020-09-01;2020-02-01T00:00:00Z;Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z",
          "2018-06-01;2020-02-01T00:00:00Z;Beer 1 validStart: 2018-01-01, availableStart: 2019-04-01T12:00:00Z",
          "2018-06-01;2019-03-01T00:00:00Z;Beer 1 validStart: 2018-01-01, availableStart: 2018-02-01T12:00:00Z"},
      delimiterString = ";")
  void getRequest_returnBeers_forPunctureDateScenarios(String validOn, String availableOn, String expected) {
    var query = String.format("{\n" + "  beers(context:{validOn: \"%s\", availableOn: \"%s\"}) {\n" + "    \tnodes {\n"
        + "        name\n" + "        soldPerYear\n" + "        brewery {\n" + "          identifier_brewery\n"
        + "          name\n" + "        }\n" + "      }\n" + "  }\n" + "}", validOn, availableOn);

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data,
        hasEntry(equalTo("beers"),
            hasEntry(equalTo("nodes"),
                hasItems(hasEntry(equalTo("name"), equalTo(expected)), hasEntry(equalTo("name"), equalTo("Beer 2")),
                    hasEntry(equalTo("name"), equalTo("Beer 3")), hasEntry(equalTo("name"), equalTo("Beer 4")),
                    hasEntry(equalTo("name"), equalTo("Beer 5")), hasEntry(equalTo("name"), equalTo("Beer 6"))))));
  }

  @Test
  void getRequest_returnBeers_forNoFieldsContext() {
    var query = "{\n" + "  beersNoFields {\n" + "    \tnodes {\n" + "        name\n" + "        soldPerYear\n"
        + "      }\n" + "  }\n" + "}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo("beersNoFields"), hasEntry(equalTo("nodes"), hasItems(
        hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2018-01-01, availableStart: 2018-02-01T12:00:00Z")),
        hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2018-01-01, availableStart: 2019-04-01T12:00:00Z")),
        hasEntry(equalTo("name"), equalTo("Beer 1 validStart: 2019-01-01, availableStart: 2019-04-01T12:00:00Z"))))));
  }

  @Test
  void postRequest_returnsBreweries_forFilterQueryWithNestedListFieldPath() {
    String query =
        "{breweries(context: {}, filter: {beers: {brewery: {name: {eq: \"Brewery X\"}}}}){ nodes { name } }}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo("breweries"), hasEntry(equalTo("nodes"), iterableWithSize(1))));
    assertThat(data, hasEntry(equalTo("breweries"),
        hasEntry(equalTo("nodes"), hasItems(hasEntry(equalTo("name"), equalTo("Brewery X"))))));
  }
}
