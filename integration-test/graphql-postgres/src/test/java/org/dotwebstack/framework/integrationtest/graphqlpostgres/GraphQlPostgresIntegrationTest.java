package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.equalToObject;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import graphql.ExecutionResult;
import graphql.GraphQL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIn;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.collection.IsMapWithSize;
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class GraphQlPostgresIntegrationTest {

  private static final String BREWERIES = "breweries";

  private static final String BREWERY = "brewery";

  private static final String BREWERYCITY = "breweryCity";

  private static final String BEERS = "beers";

  private static final String BEER = "beer";

  private static final String HISTORY = "history";

  private static final String INGREDIENTS = "ingredients";

  private static final String INGREDIENT = "ingredient";

  private static final String ROOMS = "rooms";

  private static final String GEOMETRY = "geometry";

  private static final String BEER_AGG = "beerAgg";

  private static final String INGREDIENT_AGG = "ingredientAgg";

  private static final String PART_OF = "partOf";

  private static final String NAME = "name";

  private static final String STATUS = "status";

  private static final String ERRORS = "errors";

  @Autowired
  private WebTestClient client;

  @Autowired
  private GraphQL graphQL;

  @Container
  public static final GraphQlPostgresIntegrationTest.TestPostgreSqlContainer postgreSqlContainer =
      new GraphQlPostgresIntegrationTest.TestPostgreSqlContainer().withClasspathResourceMapping("config/model",
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
    registry.add("dotwebstack.postgres.password", postgreSqlContainer::getPassword);
    registry.add("dotwebstack.postgres.database", postgreSqlContainer::getDatabaseName);
  }

  @Test
  void getRequest_returnsBeers_Default() {
    var query = "{beers{identifier_beer name since}}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    var beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get(NAME), is("Beer 1"));
    assertThat(beers.get(0)
        .get("since"),
        is(LocalDate.of(2010, 1, 1)
            .toString()));

  }

  // Using the httpController won't return a Publisher, therefore this test still uses the
  // graphQl.execute() method.
  @Test
  @SuppressWarnings("unchecked")
  void getRequest_returnsPublisher_forBeerSubscription() {
    String query = "subscription {beersSubscription{identifier_beer name}}";

    ExecutionResult result = graphQL.execute(query);

    assertThat(result.getErrors()
        .isEmpty(), is(true));

    Object publisher = result.getData();

    assertThat(publisher, Matchers.instanceOf(Publisher.class));

    List<Object> data = Flux.from((Publisher<Object>) publisher)
        .collectList()
        .block();

    assertThat(data, notNullValue());
    assertThat(data.size(), is(6));
    assertThat(data.get(0), instanceOf(ExecutionResult.class));

    ExecutionResult first = (ExecutionResult) data.get(0);
    Map<String, Object> firstData = first.getData();

    assertThat(firstData.containsKey("beersSubscription"), equalTo(true));

    Map<String, Object> beer = (Map<String, Object>) firstData.get("beersSubscription");

    assertThat(beer.entrySet(),
        equalTo(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1")
            .entrySet()));
  }

  @Test
  void getRequest_returnsBeer_forIdentifier() {
    var query = "{beer(identifier_beer: \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\" ){name}}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));

    var beer = getNestedObject(data, BEER);
    assertThat(beer.get(NAME), is("Beer 1"));
  }

  @Test
  void getRequest_returnsBeer_forIdentifier_NullIfNotExist() {
    var query = "{beer(identifier_beer: \"1111\" ){name}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data, hasEntry("beer", null));
  }

  @Test
  void getRequest_returnsBeerWithNestedObject_forIdentifier() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){ name brewery { name }}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));

    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.get(NAME), is("Beer 1"));
    assertThat(beer.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(beer, BREWERY);
    assertThat(brewery.get(NAME), is("Brewery X"));
  }

  @Test
  void getRequest_returnsBreweries_Default() {
    var query = "{breweries {name status}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);

    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0)
        .get(NAME), is("Brewery S"));
    assertThat(breweries.get(1)
        .get(STATUS), is("active"));
  }

  @Test
  void getRequest_returnsBrewery_withNestedObject() {
    String query = "{breweries{name status history{age history}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(4));

    assertThat(breweries.get(1)
        .get(NAME), is("Brewery X"));

    assertThat(breweries.get(3)
        .get(NAME), is("Brewery Z"));

    Map<String, Object> brewery = breweries.get(1);
    assertThat(brewery.containsKey(HISTORY), is(true));
    Map<String, Object> history = getNestedObject(brewery, HISTORY);
    assertThat(history.size(), is(2));
    assertThat(history.get("age"), is(1988));
    assertThat(history.get(HISTORY), is("hip and new"));

    brewery = breweries.get(3);
    assertThat(brewery.containsKey(HISTORY), is(true));
    history = getNestedObject(brewery, HISTORY);
    assertThat(history, is(nullValue()));
  }

  @Test
  void getRequest_returnsBreweriesWithMappedBy_default() {
    String query = "{breweries{name status beers{name}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0)
        .get(NAME), is("Brewery S"));
    assertThat(breweries.get(1)
        .get(STATUS), is("active"));

    List<Map<String, Object>> beers = getNestedObjects(breweries.get(0), BEERS);
    assertThat(beers.size(), is(1));

    assertThat(beers.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 6")));

    beers = getNestedObjects(breweries.get(1), BEERS);
    assertThat(beers.size(), is(3));

    assertThat(beers.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 1", "Beer 2", "Beer 4")));
  }

  @Test
  void getRequest_returnsBreweries_forSingleMappedByJoinColumn() {
    String query = "{breweries{name status beer{name}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));

    Assert.assertThat(data,
        hasEntry(equalTo("breweries"),
            IsIterableContainingInOrder.contains(hasEntry(equalTo("beer"), IsMapWithSize.aMapWithSize(1)),
                hasEntry(equalTo("beer"), IsMapWithSize.aMapWithSize(1)),
                hasEntry(equalTo("beer"), IsMapWithSize.aMapWithSize(1)),
                hasEntry(equalTo("beer"), CoreMatchers.nullValue()))));
  }

  @Test
  void getRequest_returnsBreweryWithMappedBy_forIdentifier() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name status beers{name}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.size(), is(3));
    assertThat(brewery.get(NAME), is("Brewery X"));
    assertThat(brewery.get(STATUS), is("active"));

    List<Map<String, Object>> beers = getNestedObjects(brewery, BEERS);
    assertThat(beers.size(), is(3));

    assertThat(beers.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 1", "Beer 2", "Beer 4")));
  }

  @Test
  void getRequest_returnsBreweryBatch_forIdentifier() {
    String query = "{breweryBatch (identifier_brewery : [\"id-fake1\","
        + "\"d3654375-95fa-46b4-8529-08b0f777bd6b\",\"id-fake2\"]){name status}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));

    assertThat(data.containsKey("breweryBatch"), is(true));

    var expected = new ArrayList<>();
    expected.add(null);
    expected.add(Map.of("name", "Brewery X", "status", "active"));
    expected.add(null);

    assertThat(data.get("breweryBatch"), equalTo(expected));
  }

  @Test
  void getRequest_returnsBreweryBatchList_forIdentifier() {
    String query = "{breweryBatchList (identifier_brewery : [\"id-fake1\","
        + "\"d3654375-95fa-46b4-8529-08b0f777bd6b\",\"id-fake2\"]){name status}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));

    assertThat(data.containsKey("breweryBatchList"), is(true));

    var expected = new ArrayList<>();
    expected.add(List.of());
    expected.add(List.of(Map.of("name", "Brewery X", "status", "active")));
    expected.add(List.of());

    assertThat(data.get("breweryBatchList"), equalTo(expected));
  }

  @Test
  void getRequest_returnsBreweryWithNoBeers_forIdentifier() {
    String query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c31959\"){name status beers{name}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.size(), is(3));
    assertThat(brewery.get(NAME), is("Brewery Z"));
    assertThat(brewery.get(STATUS), is("inactive"));

    List<Map<String, Object>> beers = getNestedObjects(brewery, BEERS);
    assertThat(beers, is(notNullValue()));
    assertThat(beers.size(), is(0));
  }

  @Test
  void getRequest_returnsBrewery_forIdentifierAndObjectFieldKey() {
    String query = "{breweryCity (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c31959\", city: \"Sydney\")"
        + "{name status beers{name}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERYCITY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERYCITY);
    assertThat(brewery.size(), is(3));
    assertThat(brewery.get(NAME), is("Brewery Z"));
    assertThat(brewery.get(STATUS), is("inactive"));

    List<Map<String, Object>> beers = getNestedObjects(brewery, BEERS);
    assertThat(beers, is(notNullValue()));
    assertThat(beers.size(), is(0));
  }

  @Test
  void getRequest_returnsBeersWithIngredient_forQueryWithJoinTable() {
    String query = "{beers{name ingredient{name}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));

    // assertion beer 4
    assertThat(beers.get(3)
        .get(NAME), is("Beer 4"));
    Map<String, Object> ingredientBeer4 = getNestedObject(beers.get(3), INGREDIENT);
    assertThat(ingredientBeer4.size(), is(1));
    assertThat(ingredientBeer4.get(NAME), is(IsIn.oneOf("Water", "Hop", "Barley", "Yeast")));

    // assertions beer 6
    assertThat(beers.get(5)
        .get(NAME), is("Beer 6"));

    Map<String, Object> ingredientBeer5 = getNestedObject(beers.get(5), INGREDIENT);
    assertThat(ingredientBeer5.size(), is(1));
    assertThat(ingredientBeer5.get(NAME), equalTo("Water"));
  }

  @Test
  void getRequest_returnsBeersWithDeepNesting_default() {
    String query = "{beers{identifier_beer name brewery{name beers{name ingredients{name}}}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get(NAME), is("Beer 1"));

    Map<String, Object> brewery = getNestedObject(beers.get(0), BREWERY);
    assertThat(brewery.size(), is(2));
    assertThat(brewery.get(NAME), is("Brewery X"));

    beers = getNestedObjects(brewery, BEERS);
    assertThat(beers.size(), is(3));
    assertThat(beers.get(1)
        .get(NAME), is("Beer 2"));

    List<Map<String, Object>> ingredients = getNestedObjects(beers.get(1), INGREDIENTS);
    assertThat(ingredients.size(), is(5));

    assertThat(ingredients.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Water", "Hop", "Barley", "Yeast", "Orange")));
  }

  @Test
  void getRequest_returnsBreweryWithNestedGeometry_forIdentifier() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry{type srid asWKT asWKB asGeoJSON}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(5));
    assertThat(geometry.get("type"), is("POLYGON"));
    assertThat(geometry.get("srid"), is(7415));
    assertThat(geometry.get("asWKT"),
        is("POLYGON Z((206410.1605 447480.1649 3, 206412.3799 447474.7692 3, 206418.7599 447476.6259 3,"
            + " 206417.4787 447480.3322 3, 206423.1208 447482.3191 3, 206423.0706 447482.7319 3,"
            + " 206416.4167 447480.6427 3, 206415.9896 447481.8782 3, 206410.1605 447480.1649 3))"));
    assertThat(geometry.get("asWKB"),
        is("AKAAAAMAABz3AAAAAQAAAAlBCTJRSLQ5WEEbT+Co24usQAgAAAAAAABBCTJjCgkC3kEbT8sTqSowQAg"
            + "AAAAAAABBCTKWFEZzgkEbT9KA6+36QAgAAAAAAABBCTKL1GCqZUEbT+FULDyfQAgAAAAAAABBCTK492X9i0EbT"
            + "+lGwiaBQAgAAAAAAABBCTK4kJa7mUEbT+rtdzGQQAgAAAAAAABBCTKDVWbPQkEbT+KSH/LlQAgAAAAAAABBCTJ/"
            + "6rNnoUEbT+eDRtxdQAgAAAAAAABBCTJRSLQ5WEEbT+Co24usQAgAAAAAAAA="));
    assertThat(geometry.get("asGeoJSON"),
        is("{\"type\":\"Polygon\",\"coordinates\":[[[206410.1605,447480.1649,3],[206412.3799,447474.7692,3],"
            + "[206418.7599,447476.6259,3],[206417.4787,447480.3322,3],[206423.1208,447482.3191,3],"
            + "[206423.0706,447482.7319,3],[206416.4167,447480.6427,3],[206415.9896,447481.8782,3],"
            + "[206410.1605,447480.1649,3]]]}"));
  }

  @Test
  void getRequest_returnsBreweryWithNestedGeometry_forReprojection() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(srid: 7931){type srid asWKT asWKB asGeoJSON}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(5));
    assertThat(geometry.get("type"), is("POLYGON"));
    assertThat(geometry.get("srid"), is(7931));
    assertThat(geometry.get("asWKT"),
        is("POLYGON Z((6.136068105697632 52.01329602598457 1.123, 6.136099621653557 52.01324732611223 1.123,"
            + " 6.136192828416824 52.013263421838616 1.123, 6.136174723505974 52.013296851405684 1.123,"
            + " 6.136257201433181 52.01331418524545 1.123, 6.136256530880928 52.01331789963881 1.123,"
            + " 6.136159300804138 52.01329974037945 1.123, 6.136153265833855 52.01331088356219 1.123,"
            + " 6.136068105697632 52.01329602598457 1.123))"));
    assertThat(geometry.get("asWKB"),
        is("AKAAAAMAAB77AAAAAQAAAAlAGItVcAAAAEBKAbOvJmKgP/H3ztkWhytAGItdswAAAEBKAbIWoDSrP/"
            + "H3ztkWhytAGIt2IgAAAEBKAbKdpYPUP/H3ztkWhytAGItxYwAAAEBKAbO2EvauP/H3ztkWhytAGIuHAf///"
            + "0BKAbRHexneP/H3ztkWhytAGIuG1QAAAEBKAbRmo7M2P/H3ztkWhytAGIttWAAAAEBKAbPOTvzOP/"
            + "H3ztkWhytAGItrwwAAAEBKAbQryMpNP/H3ztkWhytAGItVcAAAAEBKAbOvJmKgP/H3ztkWhys="));
    assertThat(geometry.get("asGeoJSON"),
        is("{\"type\":\"Polygon\",\"coordinates\":[[[6.136068106,52.013296026,1.123],"
            + "[6.136099622,52.013247326,1.123],[6.136192828,52.013263422,1.123],[6.136174724,52.013296851,1.123],"
            + "[6.136257201,52.013314185,1.123],[6.136256531,52.0133179,1.123],[6.136159301,52.01329974,1.123],"
            + "[6.136153266,52.013310884,1.123],[6.136068106,52.013296026,1.123]]]}"));
  }

  @Test
  void getRequest_ReturnsBreweryWithNestedGeometryAsBbox_forPersistedBbox() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(srid: 7415, bbox: true){type srid asWKT asWKB asGeoJSON}}}";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(5));
    assertThat(geometry.get("type"), is("POLYGON"));
    assertThat(geometry.get("srid"), is(7415));
    assertThat(geometry.get("asWKT"),
        is("POLYGON ((206410.1605 447474.7692, 206410.1605 447482.7319, 206423.1208 447482.7319, "
            + "206423.1208 447474.7692, 206410.1605 447474.7692))"));
    assertThat(geometry.get("asWKB"),
        is("ACAAAAMAABz3AAAAAQAAAAVBCTJRSLQ5WEEbT8sTqSowQQkyUUi0OVhBG0/q7XcxkEEJMrj3Zf2LQRtP6u13"
            + "MZBBCTK492X9i0EbT8sTqSowQQkyUUi0OVhBG0/LE6kqMA=="));
    assertThat(geometry.get("asGeoJSON"),
        is("{\"type\":\"Polygon\",\"coordinates\":[[[206410.1605,447474.7692],[206410.1605,447482.7319],"
            + "[206423.1208,447482.7319],[206423.1208,447474.7692],[206410.1605,447474.7692]]]}"));
  }

  @Test
  void getRequest_returnsProblemJson_whenCombinationTypeAndBboxInGeometryArguments() {
    var query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(type: POLYGON, srid: 7415, bbox: true){type srid asWKT asWKB asGeoJSON}}}";

    var response = WebTestClientHelper.get(client, query);

    assertThat(response,
        hasEntry("detail", "Type argument is not allowed " + "when argument bbox is true (geometry)."));
    assertThat(response, hasEntry("status", 400));
  }

  @Test
  void getRequest_returnsBreweryWithNestedGeometryAsBbox_forNonPersistedBbox() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(srid: 7931, bbox: true){type srid asWKT asWKB asGeoJSON}}}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(5));
    assertThat(geometry.get("type"), is("POLYGON"));
    assertThat(geometry.get("srid"), is(7931));
    assertThat(geometry.get("asWKT"),
        is("POLYGON ((6.136068105697632 52.01324732611223, 6.136068105697632 52.01331789963881, "
            + "6.136257201433181 52.01331789963881, 6.136257201433181 52.01324732611223, "
            + "6.136068105697632 52.01324732611223))"));
    assertThat(geometry.get("asWKB"), is("ACAAAAMAAB77AAAAAQAAAAVAGItVcAAAAEBKAbIWoDSrQBiLVXAAAABASgG0ZqOzNkAYi4cB////"
        + "QEoBtGajszZAGIuHAf///0BKAbIWoDSrQBiLVXAAAABASgGyFqA0qw=="));
    assertThat(geometry.get("asGeoJSON"),
        is("{\"type\":\"Polygon\",\"coordinates\":[[[6.136068106,52.013247326],[6.136068106,52.0133179],"
            + "[6.136257201,52.0133179],[6.136257201,52.013247326],[6.136068106,52.013247326]]]}"));
  }

  @Test
  void getRequest_returnsBreweryWithNestedGeometry_forReprojectionTo2d() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(srid: 9067){type srid asWKT asWKB asGeoJSON}}}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(5));
    assertThat(geometry.get("type"), is("POLYGON"));
    assertThat(geometry.get("srid"), is(9067));
    assertThat(geometry.get("asWKT"),
        is("POLYGON ((6.136068105697632 52.01329602598457, 6.136099621653557 52.01324732611223,"
            + " 6.136192828416824 52.013263421838616, 6.136174723505974 52.013296851405684,"
            + " 6.136257201433181 52.01331418524545, 6.136256530880928 52.01331789963881,"
            + " 6.136159300804138 52.01329974037945, 6.136153265833855 52.01331088356219,"
            + " 6.136068105697632 52.01329602598457))"));
    assertThat(geometry.get("asWKB"),
        is("ACAAAAMAACNrAAAAAQAAAAlAGItVcAAAAEBKAbOvJmKgQBiLXbMAAABASgGyFqA0q0AYi3Yi"
            + "AAAAQEoBsp2lg9RAGItxYwAAAEBKAbO2EvauQBiLhwH///9ASgG0R3sZ3kAYi4bVAAAAQEoBtGajszZ"
            + "AGIttWAAAAEBKAbPOTvzOQBiLa8MAAABASgG0K8jKTUAYi1VwAAAAQEoBs68mYqA="));
    assertThat(geometry.get("asGeoJSON"),
        is("{\"type\":\"Polygon\",\"coordinates\":[[[6.136068106,52.013296026],[6.136099622,52.013247326],"
            + "[6.136192828,52.013263422],[6.136174724,52.013296851],[6.136257201,52.013314185],"
            + "[6.136256531,52.0133179],[6.136159301,52.01329974],[6.136153266,52.013310884],"
            + "[6.136068106,52.013296026]]]}"));
  }

  @Test
  void getRequest_returnsBreweryWithGeometryType_forGeometryType() {
    var query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(type : MULTIPOLYGON){type asWKT asWKB asGeoJSON}}}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(4));
    assertThat(geometry.get("type"), is("MULTIPOLYGON"));
    assertThat(geometry.get("asWKT"),
        is("MULTIPOLYGON Z(((206410.1605 447480.1649 3, 206412.3799 447474.7692 3, 206418.7599 447476.6259 3,"
            + " 206417.4787 447480.3322 3, 206423.1208 447482.3191 3, 206423.0706 447482.7319 3,"
            + " 206416.4167 447480.6427 3, 206415.9896 447481.8782 3, 206410.1605 447480.1649 3)))"));
    assertThat(geometry.get("asWKB"),
        is("AKAAAAYAABz3AAAAAQCAAAADAAAAAQAAAAlBCTJRSLQ5WEEbT+Co24usQAgAAAAAAABBCTJjCgkC3kEbT8sTqSowQAgAAAAAAAB"
            + "BCTKWFEZzgkEbT9KA6+36QAgAAAAAAABBCTKL1GCqZUEbT+FULDyfQAgAAAAAAABBCTK492X9i0EbT+lGwiaBQAgAAAAAAABBCTK4kJ"
            + "a7mUEbT+rtdzGQQAgAAAAAAABBCTKDVWbPQkEbT+KSH/LlQAgAAAAAAABBCTJ/6rNnoUEbT+eDRtxdQAgAAAAAAABBCTJRSLQ5WEEbT"
            + "+Co24usQAgAAAAAAAA="));
    assertThat(geometry.get("asGeoJSON"),
        is("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[206410.1605,447480.1649,3],"
            + "[206412.3799,447474.7692,3],[206418.7599,447476.6259,3],[206417.4787,447480.3322,3],"
            + "[206423.1208,447482.3191,3],[206423.0706,447482.7319,3],[206416.4167,447480.6427,3],"
            + "[206415.9896,447481.8782,3],[206410.1605,447480.1649,3]]]]}"));
  }

  @Test
  void getRequest_returnsBreweryWithAggregateType_forMultipleBeers() {
    var query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) } } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));
    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(BEER_AGG), is(true));
    Map<String, Object> beerAgg = getNestedObject(brewery, BEER_AGG);
    assertThat(beerAgg.size(), is(3));
    assertThat(beerAgg.get("totalSold"), is(1700000));
    assertThat(beerAgg.get("averageSold"), is(566667));
    assertThat(beerAgg.get("maxSold"), is(1000000));
  }

  @Test
  void getRequest_returnsBreweryWithAggregateType_forSingleBeer() {
    var query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c11666\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) } } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));
    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(BEER_AGG), is(true));
    Map<String, Object> beerAgg = getNestedObject(brewery, BEER_AGG);
    assertThat(beerAgg.size(), is(3));
    assertThat(beerAgg.get("totalSold"), is(50000));
    assertThat(beerAgg.get("averageSold"), is(50000));
    assertThat(beerAgg.get("maxSold"), is(50000));
  }

  @Test
  void getRequest_returnsBreweryWithAggregateType_forNoBeer() {
    var query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c31959\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "totalCount : count( field : \"soldPerYear\" )"
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) "
        + "tastes: stringJoin( field: \"taste\" ) } } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));
    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(BEER_AGG), is(true));
    Map<String, Object> beerAgg = getNestedObject(brewery, BEER_AGG);
    assertThat(beerAgg.size(), is(5));
    assertThat(beerAgg.get("totalCount"), is(0));
    assertThat(beerAgg.get("averageSold"), is(nullValue()));
    assertThat(beerAgg.get("maxSold"), is(nullValue()));
    assertThat(beerAgg.get("tastes"), is(nullValue()));
  }

  @Test
  void getRequest_returnsBeerWithAggregateType_forIngredients() {
    var query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ totalWeight : floatSum( field : \"weight\" ) "
        + "averageWeight : floatAvg( field : \"weight\" ) maxWeight : floatMax( field : \"weight\" )"
        + "countWeight : count( field : \"weight\", distinct : false )  } } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));
    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.containsKey(INGREDIENT_AGG), is(true));
    Map<String, Object> ingredientAgg = getNestedObject(beer, INGREDIENT_AGG);
    assertThat(ingredientAgg.size(), is(4));
    assertThat(ingredientAgg.get("totalWeight"), is(22.2));
    assertThat(ingredientAgg.get("averageWeight"), is(3.7));
    assertThat(ingredientAgg.get("maxWeight"), is(6.6));
    assertThat(ingredientAgg.get("countWeight"), is(6));
  }

  @Test
  void getRequest_returnsBeerWithAggregateType_forDuplicateAvg() {
    var query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ avgA : floatAvg( field : \"weight\" ) " + "avgB : floatAvg( field : \"weight\" ) "
        + "avgC : floatAvg( field : \"weight\" )  } } }";

    Map<String, Object> data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));
    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.containsKey(INGREDIENT_AGG), is(true));
    Map<String, Object> ingredientAgg = getNestedObject(beer, INGREDIENT_AGG);
    assertThat(ingredientAgg.size(), is(3));
    assertThat(ingredientAgg.get("avgA"), is(3.7));
    assertThat(ingredientAgg.get("avgB"), is(3.7));
    assertThat(ingredientAgg.get("avgC"), is(3.7));
  }

  @Test
  void getRequest_returnsTheIngredientAndTheBeersItIsPartOf_forJoinWithReferencedColumn() {
    var query = "{ingredient(identifier_ingredient: \"cd79545c-5fbb-11eb-ae93-0242ac130002\") {name partOf{name }}}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(INGREDIENT), is(true));
    Map<String, Object> ingredient = getNestedObject(data, INGREDIENT);
    assertThat(ingredient.get(NAME), is("Caramel"));
    List<Map<String, Object>> beers = getNestedObjects(ingredient, PART_OF);
    assertThat(beers.size(), is(2));
    Map<String, Object> beer1 = beers.get(0);
    assertThat(beer1.get(NAME), is("Beer 1"));
    Map<String, Object> beer3 = beers.get(1);
    assertThat(beer3.get(NAME), is("Beer 3"));
  }

  @Test
  void getRequest_returnsTheIngredientAndTheBeersItIsPartOf_withMappedByJoinTable() {
    var query = "{ingredient(identifier_ingredient: \"cd79545c-5fbb-11eb-ae93-0242ac130002\") "
        + "{name partOfWithMappedBy{name }}}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    assertThat(data, hasEntry(equalTo("ingredient"), equalToObject(
        Map.of("name", "Caramel", "partOfWithMappedBy", List.of(Map.of("name", "Beer 1"), Map.of("name", "Beer 3"))))));
  }

  @Test
  void getRequest_returnsBeerWithAggregateType_forCountDistinct() {
    var query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ countWeightDis : count( field : \"weight\", distinct : true ) "
        + "countWeightDef : count( field : \"weight\" ) "
        + "countWeight : count( field : \"weight\", distinct : false )  } } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));
    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.containsKey(INGREDIENT_AGG), is(true));
    Map<String, Object> ingredientAgg = getNestedObject(beer, INGREDIENT_AGG);
    assertThat(ingredientAgg.size(), is(3));
    assertThat(ingredientAgg.get("countWeightDis"), is(5));
    assertThat(ingredientAgg.get("countWeightDef"), is(6));
    assertThat(ingredientAgg.get("countWeight"), is(6));
  }

  @Test
  void getRequest_returnsBeerWithStringJoinAggregateType_forString() {
    var query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ names : stringJoin( field : \"name\", separator : \"*\" )  } } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));
    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.containsKey(INGREDIENT_AGG), is(true));
    Map<String, Object> ingredientAgg = getNestedObject(beer, INGREDIENT_AGG);
    assertThat(ingredientAgg.size(), is(1));
    assertThat(ingredientAgg.get("names"), is("Water*Hop*Barley*Yeast*Orange*Caramel"));
  }

  @Test
  void getRequest_returnsBeerWithStringJoinAggregateType_forStringArray() {
    var query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name beerAgg{ totalCount : count( field : \"soldPerYear\" ) "
        + "tastes : stringJoin( field : \"taste\", distinct : true ) } } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));
    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(BEER_AGG), is(true));
    Map<String, Object> beerAgg = getNestedObject(brewery, BEER_AGG);
    assertThat(beerAgg.size(), is(2));
    assertThat(beerAgg.get("tastes"), is("FRUITY,MEATY,SMOKY,WATERY"));
    assertThat(beerAgg.get("totalCount"), is(3));
  }

  @Test
  void getRequest_returnsBeersWithStringJoinAggregateTypeWithFilterOnAggregate_forStringArray() {
    var query = "{breweries"
        + "{name beerAgg(filter: {taste: {containsAnyOf: [\"MEATY\", \"FRUITY\"]}}){ totalBeersWithTastes :"
        + " count( field : \"identifier_beer\" ) " + "tastes : stringJoin( field : \"taste\", distinct : true ) } } }";

    var data = WebTestClientHelper.get(client, query);

    Assert.assertThat(data, equalTo(Map.of(BREWERIES,
        List.of(Map.of("name", "Brewery S", "beerAgg", Map.of("totalBeersWithTastes", 1, "tastes", "MEATY,WATERY")),
            Map.of("name", "Brewery X", "beerAgg",
                Map.of("totalBeersWithTastes", 2, "tastes", "FRUITY,MEATY,SMOKY,WATERY")),
            Map.of("name", "Brewery Y", "beerAgg", Map.of("totalBeersWithTastes", 2, "tastes", "MEATY,SMOKY,SPICY")),
            Map.of("name", "Brewery Z", "beerAgg", new HashMap<String, Object>() {
              {
                put("totalBeersWithTastes", 0);
                put("tastes", null);
              }
            })))));
  }

  @Test
  void getRequest_returnsBreweryWithPostalAddressAndUnknownVisitAddress_forBreweryWithoutVisitAddres() {
    var query = "{brewery (identifier_brewery : \"6e8f89da-9676-4cb9-801b-aeb6e2a59ac9\")"
        + "{name beerAgg{ totalCount : count( field : \"soldPerYear\" ) "
        + "tastes : stringJoin( field : \"taste\", distinct : true )} " + "name postalAddress { street city } "
        + " visitAddress {street city} } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));
    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey("postalAddress"), is(true));
    Map<String, Object> postalAddress = getNestedObject(brewery, "postalAddress");
    assertThat(postalAddress.get("street"), is("5th Avenue"));
    assertThat(postalAddress.get("city"), is("New York"));
    assertThat(brewery.get("visitAddress"), is(nullValue()));
    assertThat(brewery.containsKey(BEER_AGG), is(true));
    Map<String, Object> beerAgg = getNestedObject(brewery, BEER_AGG);
    assertThat(beerAgg.size(), is(2));
    assertThat(beerAgg.get("tastes"), is("MEATY,SMOKY,SPICY"));
    assertThat(beerAgg.get("totalCount"), is(2));
  }

  @Test
  void getRequest_returnsBreweries_withStringFilter() {
    var query = "{breweries(filter: {name: {eq: \"Brewery X\"}}){ identifier_brewery name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, equalTo(Map.of("breweries",
        List.of(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")))));
  }

  @Test
  void getRequest_returnsBreweries_withBooleanFilter() {
    var query = "{breweries(filter: {multinational: { eq:true }}){ identifier_brewery name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BREWERIES), Matchers.instanceOf(List.class)));
    assertThat(data.size(), is(1));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(data.containsKey(BREWERIES), is(true));
    assertThat(breweries.get(0),
        is(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")));
  }

  @Test
  void getRequest_returnsBreweries_withBooleanNullFilter() {
    var query = "{breweries(filter: {multinational: { eq: null }}){ identifier_brewery name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BREWERIES), Matchers.instanceOf(List.class)));
    assertThat(data.size(), is(1));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(data.containsKey(BREWERIES), is(true));
    assertThat(breweries.size(), is(1));
    assertThat(breweries.get(0),
        is(Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "name", "Brewery S")));
  }

  @Test
  void getRequest_returnsBreweries_withEnumFilter() {
    var query = "{breweries(filter: {status: {eq: \"inactive\"}}){ identifier_brewery name status }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, equalTo(Map.of("breweries", List.of(Map.of("identifier_brewery",
        "28649f76-ddcf-417a-8c1d-8e5012c31959", "name", "Brewery Z", "status", "inactive")))));
  }

  @Test
  void getRequest_returnsBreweries_withCaseInsensitiveFilter() {
    var query = "{breweries(filter: {name: {eq: \"BrEwErY z\"}}){ identifier_brewery name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, equalTo(Map.of("breweries",
        List.of(Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c31959", "name", "Brewery Z")))));
  }

  @Test
  void getRequest_returnsBreweries_withNestedFilter() {
    var query = "{breweries { identifier_brewery name beers(filter: {sinceDate: {gte: \"2016-01-01\"}}) "
        + "{ identifier_beer name} }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BREWERIES), Matchers.instanceOf(List.class)));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(data.containsKey(BREWERIES), is(true));
    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0),
        is(Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "name", "Brewery S", "beers",
            List.of(Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6")))));

    assertThat(breweries.get(1),
        is(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X", "beers",
            List.of(Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4")))));

    assertThat(breweries.get(2),
        is(Map.of("identifier_brewery", "6e8f89da-9676-4cb9-801b-aeb6e2a59ac9", "name", "Brewery Y", "beers",
            List.of(Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"),
                Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5")))));

    assertThat(breweries.get(3), (is(Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c31959", "name",
        "Brewery Z", "beers", List.of()))));
  }

  @Test
  void getRequest_returnsBeers_withDateGreaterThenFilter() {
    var query = "{beers(filter: {sinceDate: {gt: \"2016-01-01\"}}){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(3));

    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6"))));
  }

  @Test
  void getRequest_returnsBeers_withDateGreaterThenEqualsFilter() {
    var query = "{beers(filter: {sinceDate: {gte: \"2016-01-01\"}}){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(4));

    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"),
            Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6"))));
  }

  @Test
  void getRequest_returnsBeers_withDateLowerThenFilter() {
    var query = "{beers(filter: {sinceDate: {lt: \"2016-01-01\"}}){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(2));

    assertThat(beers, is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
        Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"))));
  }

  @Test
  void getRequest_returnsBeers_withDateLowerThenEqualsFilter() {
    var query = "{beers(filter: {sinceDate: {lte: \"2016-01-01\"}}){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(3));
    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
            Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"),
            Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"))));
  }

  @Test
  void postRequest_returnsBeers_withDateTimeGreaterThenEqualsFilter() {
    var query = "{beers(filter: {lastBrewed: {gte: \"2020-08-11T10:15:30+01:00\"}}){ identifier_beer name }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(2));
    assertThat(beers, is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
        Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"))));
  }

  @Test
  void postRequest_returnsBreweries_withDateTimeGreaterThenEqualsFilterOnInvisibleField() {
    var query = "{breweries(filter: {created: {gte: \"2014-08-11T10:15:30+01:00\"}}){ identifier_brewery name }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data, hasEntry(equalTo(BREWERIES), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(1));
    assertThat(breweries,
        is(List.of(Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "name", "Brewery S"))));
  }

  @Test
  void postRequest_returnsError_forInvisibleFieldSelection() {
    var query = "{breweries{ identifier_brewery name created }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data, hasEntry(equalTo(ERRORS), Matchers.instanceOf(List.class)));

    List<Map<String, Object>> errors = getNestedObjects(data, ERRORS);
    assertThat(errors.size(), is(1));
    assertThat((String) errors.get(0)
        .get("message"), containsString("Field 'created' in type 'Brewery' is undefined"));
  }

  @Test
  void postRequest_returnsBeers_withMultiOperandFilter() {
    var query = "{beers(filter: {lastBrewed: {gte: \"2020-08-11T10:15:30+01:00\", lt: \"2020-09-11T10:15:30+01:00\"}})"
        + "{ identifier_beer name }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(1));
    assertThat(beers, is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"))));
  }

  @Test
  void postRequest_returnsBeers_withNotFilter() {
    var query = "{beers(filter: {lastBrewed: {not: {gte: \"2020-08-11T10:15:30+01:00\"}}}){ identifier_beer name }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(4));
    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"),
            Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6"))));
  }

  @Test
  void getRequest_returnsBeers_withNestedNotFilter() {
    var query = "{beers(filter: {brewery: {postalAddress: {city: {not: {eq: \"Dublin\"}}}}}){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(3));
    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6"))));
  }

  @Test
  void getRequest_returnsBeers_withOrFilter() {
    var query = "{\n" + "  breweries(filter: {name: {eq: \"Brewery X\"}, status: {eq: \"active\"}, "
        + "_or: {name: {eq: \"Brewery Z\"}}}) {\n" + "    name\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    Assert.assertThat(data, hasEntry(equalTo("breweries"), IsCollectionWithSize.hasSize(2)));
    Assert.assertThat(data, hasEntry(equalTo("breweries"),
        hasItems(equalToObject(Map.of("name", "Brewery X")), equalToObject(Map.of("name", "Brewery Z")))));
  }

  @Test
  void getRequest_returnsBeers_forQueryStringWithMultipleQueriesAndOperationNameProvided() {
    var query =
        "query beerCollection{beers{identifier_beer name}} query breweryCollection{breweries{identifier_brewery name}}";

    var data = WebTestClientHelper.get(client, query, "beerCollection");

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get(NAME), is("Beer 1"));
  }

  @Test
  void getRequest_returnsBeer_forParameterProvidedInVariables() {
    var query = "query singleBeer($identifier: ID!) {beer(identifier_beer: $identifier ){name}}";

    Map<String, Object> variables = Map.of("identifier", "b0e7cf18-e3ce-439b-a63e-034c8452f59c");

    var data = WebTestClientHelper.get(client, query, null, variables);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));

    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.get(NAME), is("Beer 1"));
  }

  @Test
  void getRequest_returnsProblemJson_forQueryStringWithMultipleQueriesAndOperationNameNotProvided() {
    var query =
        "query beerCollection{beers{identifier_beer name}} query breweryCollection{breweries{identifier_brewery name}}";

    var response = WebTestClientHelper.get(client, query);

    assertThat(response, hasEntry("status", 400));
    assertThat(response, hasEntry("detail", "Must provide operation name if query contains multiple operations."));
  }

  @Test
  void getRequest_returnsProblemJson_forQueryStringWithMultipleQueriesAndOperationNameIsEmptyString() {
    var query =
        "query beerCollection{beers{identifier_beer name}} query breweryCollection{breweries{identifier_brewery name}}";

    var response = WebTestClientHelper.get(client, query, "");

    assertThat(response, hasEntry("status", 400));
    assertThat(response, hasEntry("detail", "Must provide operation name if query contains multiple operations."));
  }

  @Test
  void getRequest_returnsProblemJson_forEmptyQueryString() {
    var query = "";
    var response = WebTestClientHelper.get(client, query);

    assertThat(response, hasEntry("status", 400));
    assertThat(response, hasEntry("detail", "400 BAD_REQUEST \"Required query parameter 'query' is not present.\""));
  }

  @Test
  void getRequest_returnsProblemJson_ifParameterNotProvidedInVariables() {
    var query = "query singleBeer($identifier: ID!) {beer(identifier_beer: $identifier ){name}}";
    Map<String, Object> variables = Map.of();

    var response = WebTestClientHelper.get(client, query, null, variables);

    Assert.assertThat(response,
        hasEntry(equalTo("errors"),
            IsIterableContaining.hasItems(hasEntry("message",
                "Variable 'identifier' has an invalid value: Variable 'identifier' has coerced Null value for "
                    + "NonNull type 'ID!'"))));
  }

  @Test
  void postRequestUsingContentTypeApplicationJson_returnsBeers_default() {
    var body = "{\n" + "  \"query\": \"{beers{identifier_beer name}}\",\n" + "  \"variables\": {  }\n" + "}";

    var data = WebTestClientHelper.post(client, body, MediaType.APPLICATION_JSON_VALUE);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get(NAME), is("Beer 1"));
  }

  @Test
  void postRequestUsingContentTypeApplicationJson_returnsBeers_forQueryStringWithMultipleQueriesAndOperationName() {
    var body = "{\n" + "  \"query\": \"query beerCollection{beers{identifier_beer name}}"
        + "query breweryCollection{breweries{identifier_brewery name}}\",\n"
        + "  \"operationName\": \"beerCollection\",\n" + "  \"variables\": {  }\n" + "}";

    var data = WebTestClientHelper.post(client, body, MediaType.APPLICATION_JSON_VALUE);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get(NAME), is("Beer 1"));
  }

  @Test
  void postRequestUsingContentTypeApplicationJson_returnsBeer_forParameterProvidedInVariables() {
    var body =
        "{\n" + "  \"query\": \"query singleBeer($identifier: ID!) {beer(identifier_beer: $identifier ){name}}\",\n"
            + "  \"variables\": {\n" + "      \"identifier\": \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"\n" + "      }\n"
            + "}";

    var data = WebTestClientHelper.post(client, body, MediaType.APPLICATION_JSON_VALUE);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));

    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.get(NAME), is("Beer 1"));
  }

  @Test
  void postRequest_returnsProblemJson_ifParameterNotProvidedInVariables() {
    var body =
        "{\n" + "  \"query\": \"query singleBeer($identifier: ID!) {beer(identifier_beer: $identifier ){name}}\",\n"
            + "  \"variables\": {\n" + "      \"otherVariableName\": \"otherVariableValue\"\n" + "      }\n" + "}";

    var response = WebTestClientHelper.post(client, body, MediaType.APPLICATION_JSON_VALUE);

    Assert.assertThat(response,
        hasEntry(equalTo("errors"),
            IsIterableContaining.hasItems(hasEntry("message",
                "Variable 'identifier' has an invalid value: Variable 'identifier' has coerced Null value for "
                    + "NonNull type 'ID!'"))));
  }

  @Test
  void postRequest_returnsProblemJson_forQueryStringWithMultipleQueriesAndOperationNameNotProvided() {
    var body = "{\n" + "  \"query\": \"query beerCollection{beers{identifier_beer name}}"
        + " query breweryCollection{breweries{identifier_brewery name}}\",\n" + "  \"variables\": {}\n" + "}";

    var response = WebTestClientHelper.post(client, body, MediaType.APPLICATION_JSON_VALUE);

    assertThat(response, hasEntry("status", 400));
    assertThat(response, hasEntry("detail", "Must provide operation name if query contains multiple operations."));
  }

  @Test
  void postRequest_returnsProblemJson_forEmptyQueryString() {
    var body = "{\n" + "  \"query\": \"\",\n" + "  \"operationName\": \"\",\n" + "  \"variables\": {}\n" + "}";

    var response = WebTestClientHelper.post(client, body, MediaType.APPLICATION_JSON_VALUE);

    assertThat(response, hasEntry("status", 400));
    assertThat(response, hasEntry("detail", "Required parameter 'query' can not be empty."));
  }

  @Test
  void postRequest_returnsProblemJson_forMissingQueryString() {
    var body = "{\n" + "  \"operationName\": \"\",\n" + "  \"variables\": {}\n" + "}";

    var response = WebTestClientHelper.post(client, body, MediaType.APPLICATION_JSON_VALUE);

    assertThat(response, hasEntry("status", 400));
    assertThat(response, hasEntry("detail", "Required parameter 'query' is not present."));
  }

  @Test
  void getRequest_returnsBeers_forFilterQueryWithNestedFieldPath() {
    String query = "{beers(filter: {brewery: {visitAddress: {city: {eq: \"Dublin\"}}}}){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(3));
    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
            Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"),
            Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4"))));
  }

  @Test
  void getRequest_returnsBreweries_forFilterQueryWithNestedListFieldPath() {
    var query = "{breweries(filter: {beers: {brewery: {name: {eq: \"Brewery X\"}}}}){ identifier_brewery name }}";

    var data = WebTestClientHelper.get(client, query);

    Assert.assertThat(data, hasEntry(equalTo("breweries"), iterableWithSize(1)));
    Assert.assertThat(data, hasEntry(equalTo("breweries"), hasItems(hasEntry(equalTo("name"), equalTo("Brewery X")))));
  }

  @Test
  void getRequest_returnsBeers_forSortNameDescQuery() {
    var query = "{beers(sort: NAME_DESC){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5"),
            Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4"),
            Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"),
            Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"),
            Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"))));
  }

  @Test
  void getRequest_returnsIngredients_forSortNameOnQuery() {
    var query = "{ingredients(sort: NAME){ name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(INGREDIENTS), is(true));

    List<Map<String, Object>> ingredients = getNestedObjects(data, INGREDIENTS);
    assertThat(ingredients.size(), is(6));
    assertThat(ingredients, is(List.of(Map.of("name", "Barley"), Map.of("name", "Caramel"), Map.of("name", "Hop"),
        Map.of("name", "Orange"), Map.of("name", "Water"), Map.of("name", "Yeast"))));
  }

  @Test
  void getRequest_returnsBeers_forSortQueryWithNestedFieldPath() {
    var query = "{beers(sort: BREWERY_CITY){ identifier_beer name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers,
        is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
            Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"),
            Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4"),
            Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5"),
            Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6"))));
  }

  @Test
  void getRequest_returnsBreweries_forNestedSortQuery() {
    var query = "{breweries { identifier_brewery name beers(sort: NAME_DESC){ identifier_beer name }} }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(4));

    assertThat(breweries, IsIterableContainingInOrder.contains(hasEntry("name", "Brewery S"),
        hasEntry("name", "Brewery X"), hasEntry("name", "Brewery Y"), hasEntry("name", "Brewery Z")));

    var beers = getNestedObjects(breweries.get(1), BEERS);

    assertThat(beers, IsIterableContainingInOrder.contains(hasEntry("name", "Beer 4"), hasEntry("name", "Beer 2"),
        hasEntry("name", "Beer 1")));
  }

  @Test
  void getRequest_returnsBreweries_forNestedFilterQuery() {
    var query =
        "{breweries { identifier_brewery name beers(filter: {brewery: {visitAddress: {city: {eq: \"Dublin\"}}}})"
            + "{ identifier_beer name }} }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(4));

    assertThat(breweries, IsIterableContainingInOrder.contains(hasEntry("name", "Brewery S"),
        hasEntry("name", "Brewery X"), hasEntry("name", "Brewery Y"), hasEntry("name", "Brewery Z")));

    assertThat(getNestedObjects(breweries.get(0), BEERS).size(), is(0));
    assertThat(getNestedObjects(breweries.get(1), BEERS).size(), is(3));
    assertThat(getNestedObjects(breweries.get(2), BEERS).size(), is(0));
    assertThat(getNestedObjects(breweries.get(3), BEERS).size(), is(0));
  }

  @Test
  void getRequest_returnsBreweries_forNestedObjectFilterQuery() {
    var query = "{breweries(filter: {history: {age: {eq: 1988}}}) { identifier_brewery name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(1));

    assertThat(breweries, IsIterableContainingInOrder.contains(hasEntry("name", "Brewery X")));
  }

  @Test
  void getRequest_returnsBreweries_forDefaultGeometryFilterQueryWkt() {
    var query = "{breweries(filter: {geometry: {srid: 28992, intersects: {fromWKT: "
        + "\"POLYGON((206387.0439 447771.0547, 206384.4262 447765.9768, 206389.6081 447763.4587, "
        + "206392.4175 447767.804, 206391.3745 447770.732, 206387.0439 447771.0547))\"}}})"
        + " { identifier_brewery name geometry { srid asWKT} }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(1));

    assertThat(breweries, IsIterableContainingInOrder.contains(hasEntry("name", "Brewery S")));
  }

  @Test
  void postRequest_returnsBreweries_forDefaultGeometryFilterQueryWkb() {
    var query = "{breweries(filter: {geometry: {intersects: {fromWKB: \"AQMAAAABAAAABgAAAEI+6FmYMQlB3EYDOGxUG0Gsi9togzE"
        + "JQVtCPuhXVBtBZohj3awxCUHrc7XVTVQbQXE9ClfDMQlBqMZLN19UG0Ej2/n+ujEJQXNoke1qVBtBQj7oWZgxCUHcRgM4bFQbQQ"
        + "==\"}}}) { identifier_brewery name }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(1));

    assertThat(breweries, IsIterableContainingInOrder.contains(hasEntry("name", "Brewery S")));
  }

  @Test
  void postRequest_returnsBreweries_forGeometryFilterQueryType() {
    var query = "{breweries(filter: {geometry: {type: POLYGON}, _or: { geometry: {type: MULTIPOLYGON}}})"
        + " { identifier_brewery name }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(4));
  }

  @Test
  void postRequest_returnsBreweries_forDefaultGeometryFilterQueryGeoJson() {
    var query = "{breweries(filter: {geometry: {intersects: {fromGeoJSON: \"{\\\"type\\\": \\\"Polygon\\\", "
        + "\\\"coordinates\\\": [[[206387.0439,447771.0547],[206384.4262,447765.9768],[206389.6081,447763.4587],"
        + "[206392.4175,447767.804],[206391.3745,447770.732],[206387.0439,447771.0547]]],"
        + "\\\"crs\\\":{\\\"type\\\":\\\"name\\\",\\\"properties\\\":"
        + "{\\\"name\\\":\\\"EPSG:28992\\\"}}}\"}}}) { identifier_brewery name }}";

    var data = WebTestClientHelper.post(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(1));

    assertThat(breweries, IsIterableContainingInOrder.contains(hasEntry("name", "Brewery S")));
  }

  @Test
  void postRequest_returnsBadRequest_forInvalidGeometryFilterQueryGeoJson() {
    var query = "{breweries(filter: {geometry: {intersects: {fromGeoJSON: \"{\\\"type\\\": \\\"Polygon\\\", "
        + "\\\"coordinates\\\": [[206387.0439,447771.0547],[206384.4262,447765.9768],[206389.6081,447763.4587],"
        + "[206392.4175,447767.804],[206391.3745,447770.732],[206387.0439,447771.0547]]],"
        + "\\\"crs\\\":{\\\"type\\\":\\\"name\\\",\\\"properties\\\":"
        + "{\\\"name\\\":\\\"EPSG:28992\\\"}}}\"}}}) { identifier_brewery name }}";

    var result = WebTestClientHelper.post(client, query);
    assertThat(result.get("status"), is(HttpStatus.BAD_REQUEST.value()));
    assertThat(result.get("detail"), is("The filter input GeoJSON is invalid!"));
  }

  // insersect
  @Test
  void getRequest_returnsRooms_forSegmentsIntersectsGeometryFilter() {
    var geoAsWkt = "MultiPolygon (((216092.41684153329697438 568768.15405663626734167, "
        + "216050.95043135021114722 567959.55905806645750999, 215221.62222768887295388 568477.88918535481207073, "
        + "216092.41684153329697438 568768.15405663626734167)))";
    var query =
        "{rooms(filter: {geometry: {srid: 28992, intersects: {fromWKT: " + "\"" + geoAsWkt + "\"}}})" + " { name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(ROOMS), is(true));

    List<Map<String, Object>> rooms = getNestedObjects(data, ROOMS);
    assertThat(rooms.size(), is(1));

    assertThat(rooms, IsIterableContainingInOrder.contains(hasEntry("name", "Room 1")));
  }

  @Test
  void getRequest_returnsRooms_forSegmentsWithinGeometryFilter() {
    var geoAsWkt = "MultiPolygon (((210909.25174733690801077 570909.87031132145784795, "
        + "211281.18978613000945188 570894.99278976966161281, 211087.78200595758971758 570359.40201390767470002, "
        + "210909.25174733690801077 570909.87031132145784795)))";

    var query =
        "{rooms(filter: {geometry: {srid: 28992, within: {fromWKT: " + "\"" + geoAsWkt + "\"}}})" + " { name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(ROOMS), is(true));

    List<Map<String, Object>> rooms = getNestedObjects(data, ROOMS);
    assertThat(rooms.size(), is(1));

    assertThat(rooms, IsIterableContainingInOrder.contains(hasEntry("name", "Room 1")));
  }

  @Test
  void getRequest_returnsRooms_forSegmentsContainsGeometryFilter() {
    var geoAsWkt = "MultiPolygon (((6.18330167358269112 53.16425784117628695, 6.35888371684041154 53.16421120088322283,"
        + "6.36187797012836054 53.08195886431762034, 6.14040884350146321 53.08591436347985848, "
        + "6.18330167358269112 53.16425784117628695)))";

    var query =
        "{rooms(filter: {geometry: {srid: 28992, contains: {fromWKT: " + "\"" + geoAsWkt + "\"}}})" + " { name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(ROOMS), is(true));

    List<Map<String, Object>> rooms = getNestedObjects(data, ROOMS);
    assertThat(rooms.size(), is(1));

    assertThat(rooms, IsIterableContainingInOrder.contains(hasEntry("name", "Room 1")));
  }

  @Test
  void getRequest_returnsRooms_forSegmentsTouchesGeometryFilter() {
    var geoAsWkt = "POINT(214593.046 570933.057)";
    var query =
        "{rooms(filter: {geometry: {srid: 28992, touches: {fromWKT: " + "\"" + geoAsWkt + "\"}}})" + " { name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(ROOMS), is(true));

    List<Map<String, Object>> rooms = getNestedObjects(data, ROOMS);
    assertThat(rooms.size(), is(1));

    assertThat(rooms, IsIterableContainingInOrder.contains(hasEntry("name", "Room 1")));
  }

  @Test
  void getRequest_returnsBreweries_forStringPartialFilter() {
    var query = "{breweries(filter: {postalAddress: {streetPartial: {match: \"Ch\"}}}) { name }}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data, equalTo(Map.of("breweries", List.of(Map.of("name", "Brewery X")))));
  }

  @Test
  void getRequest_returnsBeers_forStringListPartialFilter() {
    var query = """
          {
            beers(
              filter: {
                partialSecretIngredient: {
                  match: "li"
                }
              }
            ) {
              name
            }
          }
        """;

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data, equalTo(
        Map.of("beers", List.of(Map.of("name", "Beer 1"), Map.of("name", "Beer 2"), Map.of("name", "Beer 6")))));
  }


  @Test
  void getRequest_returnsBreweries_forEnumListFilter() {
    var query =
        "{\n" + "  beers(filter: {taste: {containsAllOf: [\"MEATY\", \"FRUITY\"]}}) {\n" + "    name\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data, equalTo(Map.of("beers", List.of(Map.of("name", "Beer 1"), Map.of("name", "Beer 2")))));
  }

  @Test
  void getRequest_returnsBeersWithIngredients_forQueryWithJoinTable() {
    var query = "{\n" + "  beers {\n" + "    identifier_beer\n" + "    name\n" + "    ingredients{\n"
        + "      identifier_ingredient\n" + "      name\n" + "    }\n" + "  }\n" + "}\n";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data.size(), is(1));
    assertThat(data,
        equalTo(
            Map.of("beers",
                List.of(
                    Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1", "ingredients",
                        List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"),
                            Map.of("identifier_ingredient", "cd79538a-5fbb-11eb-ae93-0242ac130002", "name", "Orange"),
                            Map.of("identifier_ingredient", "cd79545c-5fbb-11eb-ae93-0242ac130002", "name",
                                "Caramel"))),
                    Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2", "ingredients",
                        List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"),
                            Map.of("identifier_ingredient", "cd79538a-5fbb-11eb-ae93-0242ac130002", "name", "Orange"))),
                    Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3", "ingredients",
                        List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"),
                            Map.of("identifier_ingredient", "cd79545c-5fbb-11eb-ae93-0242ac130002", "name",
                                "Caramel"))),
                    Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4", "ingredients",
                        List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"))),
                    Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5", "ingredients",
                        List.of(
                            Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name", "Water"),
                            Map.of("identifier_ingredient", "cd794c14-5fbb-11eb-ae93-0242ac130002", "name", "Hop"),
                            Map.of("identifier_ingredient", "cd795196-5fbb-11eb-ae93-0242ac130002", "name", "Barley"),
                            Map.of("identifier_ingredient", "cd795191-5fbb-11eb-ae93-0242ac130002", "name", "Yeast"))),
                    Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6", "ingredients",
                        List.of(Map.of("identifier_ingredient", "cd795192-5fbb-11eb-ae93-0242ac130002", "name",
                            "Water")))))));
  }

  @Test
  void getRequest_returnsBeers_forQueryWithBreweryNestedFieldFilter() {
    var query = "{\n" + "  beers(filter: {brewery: {history: {age: {eq: 1988}}}}) {\n" + "    name\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    Assert.assertThat(data, hasEntry(equalTo("beers"), hasItems(hasEntry(equalTo("name"), equalTo("Beer 1")),
        hasEntry(equalTo("name"), equalTo("Beer 2")), hasEntry(equalTo("name"), equalTo("Beer 4")))));
  }

  @Test
  void getRequest_returnsBreweries_withNestedObjectExistsTrueFilter() {
    var query = "{\n" + "  breweries(filter: {postalAddress: {_exists: true}}) {\n" + "    name\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    Assert.assertThat(data, hasEntry(equalTo("breweries"),
        hasItems(hasEntry(equalTo("name"), equalTo("Brewery X")), hasEntry(equalTo("name"), equalTo("Brewery Y")))));
  }

  @Test
  void getRequest_returnsBreweries_withNestedObjectExistsFalseFilter() {
    var query = "{\n" + "  breweries(filter: {visitAddress: {_exists: false}}) {\n" + "    name\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    Assert.assertThat(data, hasEntry(equalTo("breweries"), hasItems(hasEntry(equalTo("name"), equalTo("Brewery S")),
        hasEntry(equalTo("name"), equalTo("Brewery Y")), hasEntry(equalTo("name"), equalTo("Brewery Z")))));
  }

  @Test
  void getRequest_returnsBreweries_withObjectExistsFilter() {
    var query = "{\n" + "  breweries(filter: {_exists: false}) {\n" + "    name\n" + "  }\n" + "}";

    var response = WebTestClientHelper.get(client, query);

    assertThat(response, hasEntry("detail", "Filter operator '_exists' is only supported for nested objects"));
    assertThat(response, hasEntry("status", 400));
  }

  @Test
  void getRequest_returnsBeerWithBrewery_withMultipleAliasesForBreweryObject() {
    var query = "query beer{\n" + "  beer(identifier_beer:\"b0e7cf18-e3ce-439b-a63e-034c8452f59c\") {\n"
        + "    identifier_beer\n" + "    name\n" + "  \tbrewery1: brewery{\n" + "      identifier_brewery\n" + "    }\n"
        + "    brewery2: brewery{\n" + "      name\n" + "    }\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    Assert.assertThat(data,
        hasEntry(equalTo(BEER),
            equalTo(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1", "brewery1",
                Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b"), "brewery2",
                Map.of("name", "Brewery X")))));
  }

  @Test
  void getRequest_returnsBreweryWithBeers_withMultipleAliasesForBeersObjects() {
    var query = "query brewery {\n" + "  brewery (identifier_brewery: \"d3654375-95fa-46b4-8529-08b0f777bd6b\") {\n"
        + "    identifier_brewery\n" + "  \tsmokybeers: beers(filter: {taste: {containsAnyOf: [\"SMOKY\"]}}){\n"
        + "      name\n" + "      taste\n" + "    }\n"
        + "  \tfruityBeers: beers(filter: {taste: {containsAnyOf: [\"FRUITY\"]}}){\n" + "      name\n" + "      taste\n"
        + "    }\n" + "  }\n" + "}";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    Assert.assertThat(data,
        hasEntry(equalTo(BREWERY),
            equalTo(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "fruityBeers",
                List.of(Map.of("name", "Beer 1", "taste", List.of("MEATY", "FRUITY")),
                    Map.of("name", "Beer 2", "taste", List.of("MEATY", "SMOKY", "WATERY", "FRUITY"))),
                "smokybeers",
                List.of(Map.of("name", "Beer 2", "taste", List.of("MEATY", "SMOKY", "WATERY", "FRUITY")))))));
  }

  @Test
  void getRequest_returnsBeerWithIngredients_withAliasesForJoinTable() {
    var query = "query beerIngredients{\n" + "    beer(identifier_beer: \"766883b5-3482-41cf-a66d-a81e79a4f0ed\"){\n"
        + "      identifier_beer\n" + "      ingName: ingredients{\n" + "          name\n" + "      }\n" + "    }\n"
        + "  }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    Assert.assertThat(data, hasEntry(equalTo(BEER), equalTo(Map.of("identifier_beer",
        "766883b5-3482-41cf-a66d-a81e79a4f0ed", "ingName",
        List.of(Map.of("name", "Water"), Map.of("name", "Hop"), Map.of("name", "Barley"), Map.of("name", "Yeast"))))));
  }

  @Test
  void getRequest_returnsBeersWithIngredients_withAliasesForJoinTableAndSortableByOnNestedObject() {
    var query = "query beerIngredients{\n" + "    breweries(sort: BREWERY_AGE){\n" + "      identifier_brewery\n"
        + "      name\n" + "      his: history{\n" + "          leeftijd: age\n" + "      }\n" + "    }\n" + "  }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, aMapWithSize(1));
    Assert.assertThat(data, hasEntry(equalTo(BREWERIES), hasItems(equalTo(new HashMap<String, Object>() {
      {
        put("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c31959");
        put("name", "Brewery Z");
        put("his", null);
      }
    }), equalTo(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X", "his",
        Map.of("leeftijd", 1988))),
        equalTo(Map.of("identifier_brewery", "6e8f89da-9676-4cb9-801b-aeb6e2a59ac9", "name", "Brewery Y", "his",
            Map.of("leeftijd", 1900))),
        equalTo(Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "name", "Brewery S", "his",
            Map.of("leeftijd", 1600))))));
  }

  @Test
  void getRequest_returnsBreweryWithCustomValue_forSingleBeer() {
    var query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c11666\")"
        + "{identifier_brewery shortName } }";

    var data = WebTestClientHelper.get(client, query);

    assertThat(data, equalTo(
        Map.of("brewery", Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "shortName", "Bre"))));
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
