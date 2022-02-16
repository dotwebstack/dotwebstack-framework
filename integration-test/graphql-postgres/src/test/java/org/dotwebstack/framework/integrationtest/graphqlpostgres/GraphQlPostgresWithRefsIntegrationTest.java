package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.dotwebstack.framework.integrationtest.graphqlpostgres.Assert.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.equalToObject;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import graphql.GraphQL;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.collection.IsCollectionWithSize;
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
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("with-refs")
@AutoConfigureWebTestClient
@Testcontainers
class GraphQlPostgresWithRefsIntegrationTest {

  @Autowired
  private WebTestClient client;

  @Autowired
  private GraphQL graphQL;

  @Container
  static final GraphQlPostgresWithRefsIntegrationTest.TestPostgreSqlContainer postgreSqlContainer =
      new GraphQlPostgresWithRefsIntegrationTest.TestPostgreSqlContainer().withClasspathResourceMapping("config/model",
          "/docker-entrypoint-initdb.d", BindMode.READ_ONLY);

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
  void getRequest_returnsBeersWithBreweryRef_withJoinColumn() {
    var query = "{\n" + "  beerCollection {\n" + "    name\n" + "    brewery {\n" + "      ref {\n"
        + "        identifier_brewery\n" + "      }\n" + "    }\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("beerCollection"), IsCollectionWithSize.hasSize(6)));
    assertThat(data, hasEntry(equalTo("beerCollection"), hasItems(equalToObject(Map.of("name", "Beer 1", "brewery",
        Map.of("ref", Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b")))))));
  }

  @Test
  void getRequest_returnsBeersWithPredecessor_withJoinColumn() {
    var query = "{\n" + "  beerCollection {\n" + "    name\n" + "    predecessor {\n" + "      ref {\n"
        + "        identifier_beer\n" + "      }\n" + "    }\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("beerCollection"), IsCollectionWithSize.hasSize(6)));
    assertThat(data, hasEntry(equalTo("beerCollection"),
        hasItems(allOf(hasEntry(equalTo("name"), equalTo("Beer 1")), hasEntry(equalTo("predecessor"), nullValue())))));
    assertThat(data, hasEntry(equalTo("beerCollection"), hasItems(
        allOf(hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("predecessor"), notNullValue())))));
  }

  @Test
  void getRequest_returnsBeersWithBreweryNode_withJoinColumn() {
    var query = "{\n" + "  beerCollection {\n" + "    name\n" + "    brewery {\n" + "      node {\n" + "        name\n"
        + "      }\n" + "    }\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("beerCollection"), IsCollectionWithSize.hasSize(6)));
    assertThat(data, hasEntry(equalTo("beerCollection"),
        hasItems(equalToObject(Map.of("name", "Beer 1", "brewery", Map.of("node", Map.of("name", "Brewery X")))))));
  }

  @Test
  void getRequest_returnsBeersWithIngredientRefs_withJoinTable() {
    var query = "{\n" + "  beerCollection {\n" + "    name\n" + "    ingredients {\n" + "    \trefs {\n"
        + "        code\n" + "      }\n" + "    }\n" + "    \n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("beerCollection"), IsCollectionWithSize.hasSize(6)));
    assertThat(data, hasEntry(equalTo("beerCollection"),
        hasItems(equalToObject(
            Map.of("name", "Beer 1", "ingredients", Map.of("refs", List.of(Map.of("code", "WTR"), Map.of("code", "HOP"),
                Map.of("code", "BRL"), Map.of("code", "YST"), Map.of("code", "RNG"), Map.of("code", "CRM"))))))));
  }

  @Test
  void getRequest_returnsBeersWithIngredientNodes_withBatchLoadManyJoinTable() {
    var query = "{\n" + "  beerCollection {\n" + "    name\n" + "    ingredients {\n" + "    \tnodes {\n"
        + "        name\n" + "        code\n" + "      }\n" + "    }\n" + "    \n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("beerCollection"), IsCollectionWithSize.hasSize(6)));
    assertThat(data,
        hasEntry(equalTo("beerCollection"),
            hasItems(equalToObject(Map.of("name", "Beer 1", "ingredients",
                Map.of("nodes",
                    List.of(Map.of("name", "Water", "code", "WTR"), Map.of("name", "Hop", "code", "HOP"),
                        Map.of("name", "Barley", "code", "BRL"), Map.of("name", "Yeast", "code", "YST"),
                        Map.of("name", "Orange", "code", "RNG"), Map.of("name", "Caramel", "code", "CRM"))))))));
  }

  @Test
  void getRequest_returnsBeers_withJoinColumnFilterOnReferenceObject() {
    var query = "{\n" + "  beerCollection(filter: {brewery: {ref: {identifier_brewery: {"
        + "eq: \"d3654375-95fa-46b4-8529-08b0f777bd6b\"}}}}) {\n" + "    name\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("beerCollection"), hasItems(equalTo(Map.of("name", "Beer 1")),
        equalTo(Map.of("name", "Beer 2")), equalTo(Map.of("name", "Beer 4")))));
  }

  @Test
  void getRequest_returnsBeers_withJoinTableFilterOnReferenceObject() {
    var query = "{\n" + "  beerCollection(filter: {ingredients: {refs: {code: {eq: \"CRM\"}}}}) {\n" + "    name\n"
        + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("beerCollection"),
        hasItems(equalTo(Map.of("name", "Beer 1")), equalTo(Map.of("name", "Beer 3")))));
  }

  @Test
  void getRequest_returnsBeerWithBrewery_withMultipleAliasesForBreweryObject() {
    var query = "query beer{\n" + "  beer(identifier_beer:\"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){\n"
        + "    identifier_beer\n" + "    name\n" + "    brewery1: brewery{\n" + "      node{\n"
        + "        identifier_brewery\n" + "      }\n" + "    }\n" + "    brewery2: brewery{\n" + "      node{\n"
        + "        name\n" + "      }\n" + "    }\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey("beer"), is(true));

    var beer = getNestedObject(data, "beer");

    assertThat(beer.size(), is(4));
    assertThat(beer.get("name"), is("Beer 1"));
    assertThat(beer.get("identifier_beer"), is("b0e7cf18-e3ce-439b-a63e-034c8452f59c"));
    assertThat(beer.containsKey("brewery1"), is(true));
    assertThat(beer.containsKey("brewery2"), is(true));

    var brewery1 = getNestedObject(getNestedObject(beer, "brewery1"), "node");
    assertThat(brewery1.size(), is(1));
    assertThat(brewery1.containsKey("identifier_brewery"), is(true));
    assertThat(brewery1.get("identifier_brewery"), is("d3654375-95fa-46b4-8529-08b0f777bd6b"));

    var brewery2 = getNestedObject(getNestedObject(beer, "brewery2"), "node");
    assertThat(brewery2.size(), is(1));
    assertThat(brewery2.containsKey("name"), is(true));
    assertThat(brewery2.get("name"), is("Brewery X"));
  }

  @Test
  void getRequest_returnsBreweryWithBeers_withMultipleAliasesForBeersObjects() {
    var query = "query brewery {\n" + "  brewery (identifier_brewery: \"d3654375-95fa-46b4-8529-08b0f777bd6b\") {\n"
        + "    identifier_brewery\n" + "  \tsmokybeers: beers(filter: {taste: {containsAnyOf: [\"SMOKY\"]}}){\n"
        + "      name\n" + "      taste\n" + "    }\n"
        + "  \tfruityBeers: beers(filter: {taste: {containsAnyOf: [\"FRUITY\"]}}){\n" + "      name\n" + "      taste\n"
        + "    }\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey("brewery"), is(true));

    var brewery = getNestedObject(data, "brewery");

    assertThat(brewery.size(), is(3));
    assertThat(brewery.get("identifier_brewery"), is("d3654375-95fa-46b4-8529-08b0f777bd6b"));
    assertThat(brewery.containsKey("fruityBeers"), is(true));
    assertThat(brewery.containsKey("smokybeers"), is(true));

    var fruityBeers = getNestedObjects(brewery, "fruityBeers");
    assertThat(fruityBeers.size(), is(2));

    var fruityBeer1 = fruityBeers.get(0);
    assertThat(fruityBeer1.containsKey("name"), is(true));
    assertThat(fruityBeer1.get("name"), is("Beer 1"));
    assertThat(fruityBeer1.get("taste"), is(List.of("MEATY", "FRUITY")));

    var fruityBeer2 = fruityBeers.get(1);
    assertThat(fruityBeer2.containsKey("name"), is(true));
    assertThat(fruityBeer2.get("name"), is("Beer 2"));
    assertThat(fruityBeer2.get("taste"), is(List.of("MEATY", "SMOKY", "WATERY", "FRUITY")));

    var smokybeers = getNestedObjects(brewery, "smokybeers");
    assertThat(smokybeers.size(), is(1));

    var smokyBeer1 = smokybeers.get(0);
    assertThat(smokyBeer1.containsKey("name"), is(true));
    assertThat(smokyBeer1.get("name"), is("Beer 2"));
    assertThat(smokyBeer1.get("taste"), is(List.of("MEATY", "SMOKY", "WATERY", "FRUITY")));
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getNestedObjects(Map<String, Object> data, String name) {
    return (List<Map<String, Object>>) data.get(name);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getNestedObject(Map<String, Object> data, String name) {
    return (Map<String, Object>) data.get(name);
  }
}
