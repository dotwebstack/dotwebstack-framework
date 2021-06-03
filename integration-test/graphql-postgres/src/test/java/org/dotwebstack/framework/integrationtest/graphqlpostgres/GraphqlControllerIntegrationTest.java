package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.r2dbc.spi.ConnectionFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIn;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
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
import reactor.core.publisher.Flux;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class GraphqlControllerIntegrationTest {

  private static final String BREWERIES = "breweries";

  private static final String BREWERY = "brewery";

  private static final String BEERS = "beers";

  private static final String BEER = "beer";

  private static final String HISTORY = "history";

  private static final String INGREDIENTS = "ingredients";

  private static final String INGREDIENT = "ingredient";

  private static final String GEOMETRY = "geometry";

  private static final String BEER_AGG = "beerAgg";

  private static final String INGREDIENT_AGG = "ingredientAgg";

  private static final String PART_OF = "partOf";

  private static final String ERRORS = "errors";

  private static final String NAME = "name";

  private static final String STATUS = "status";

  @Autowired
  private WebTestClient client;

  @Autowired
  private GraphQL graphQL;

  private final ObjectMapper mapper = new ObjectMapper();

  @Container
  static GraphqlControllerIntegrationTest.TestPostgreSqlContainer postgreSqlContainer =
      new GraphqlControllerIntegrationTest.TestPostgreSqlContainer().withClasspathResourceMapping("config/model",
          "/docker-entrypoint-initdb.d", BindMode.READ_ONLY);

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

  @org.springframework.boot.test.context.TestConfiguration
  static class TestConfiguration {
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
      ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
      initializer.setConnectionFactory(connectionFactory);

      return initializer;
    }
  }

  @Test
  void getRequest_ReturnsBeers_Default() {
    var query = "{beers{identifier_beer name}}";
    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get(NAME), is("Beer 1"));
  }

  // TODO: Fix publisher test to use webTestClient
  @Test
  @SuppressWarnings("unchecked")
  void getRequest_ReturnsPublisher_forBeerSubscription() {
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
  void getRequest_ReturnsBeer_forIdentifier() {
    var query = "{beer(identifier_beer: \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\" ){name}}";
    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));

    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.get(NAME), is("Beer 1"));
  }

  @Test
  void getRequest_ReturnsBeerWithNestedObject_forIdentifier() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){ name brewery { name }}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));

    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.get(NAME), is("Beer 1"));
    assertThat(beer.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(beer, BREWERY);
    assertThat(brewery.get(NAME), is("Brewery X"));
  }

  @Test
  void getRequest_ReturnsBreweries_Default() {
    var query = "{breweries {name status}}";
    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);

    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0)
        .get(NAME), is("Brewery X"));
    assertThat(breweries.get(1)
        .get(STATUS), is("active"));
  }

  @Test
  void getRequest_ReturnsBrewery_withNestedObject() {
    String query =
        "{brewery(identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name status history{age history}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(HISTORY), is(true));
    Map<String, Object> history = getNestedObject(brewery, HISTORY);
    assertThat(history.size(), is(2));
    assertThat(history.get("age"), is(1988));
    assertThat(history.get(HISTORY), is("hip and new"));
  }

  @Test
  void getRequest_returnsBreweriesrWithMappedBy_default() {
    String query = "{breweries{name status beers{name}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERIES), is(true));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0)
        .get(NAME), is("Brewery X"));
    assertThat(breweries.get(1)
        .get(STATUS), is("active"));

    List<Map<String, Object>> beers = getNestedObjects(breweries.get(0), BEERS);
    assertThat(beers.size(), is(3));

    assertThat(beers.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 1", "Beer 2", "Beer 4")));

    beers = getNestedObjects(breweries.get(1), BEERS);
    assertThat(beers.size(), is(2));

    assertThat(beers.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 3", "Beer 5")));
  }

  @Test
  void getRequest_returnsBreweryWithMappedBy_forIdentifier() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name status beers{name}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_returnsBreweryWithNoBeers_forIdentifier() {
    String query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c31959\"){name status beers{name}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_returnsBeersWithIngredients_forQueryWithJoinTable() {
    String query = "{beers{name ingredients{name}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get(NAME), is("Beer 1"));

    List<Map<String, Object>> ingredients = getNestedObjects(beers.get(0), INGREDIENTS);
    assertThat(ingredients.size(), is(6));

    assertThat(ingredients.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Water", "Hop", "Barley", "Yeast", "Orange", "Caramel")));

    ingredients = getNestedObjects(beers.get(3), INGREDIENTS);
    assertThat(ingredients.size(), is(4));

    assertThat(ingredients.stream()
        .map(map -> map.get(NAME))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Water", "Hop", "Barley", "Yeast")));
  }

  @Test
  void getRequest_returnsBeersWithIngredient_forQueryWithJoinTable() {
    String query = "{beers{name ingredient{name}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_ReturnsBreweryWithNestedGeometry_forIdentifier() {
    String query =
        "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name geometry{type asWKT asWKB}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(3));
    assertThat(geometry.get("type"), is("POINT"));
    assertThat(geometry.get("asWKT"), is("POINT (5.979274334569982 52.21715768613606)"));
    assertThat(geometry.get("asWKB"), is("00000000014017eac6e4232933404a1bcbd2b403c4"));
  }

  @Test
  void getRequest_ReturnsBreweryWithGeometryType_forGeometryType() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(type : MULTIPOINT){type asWKT asWKB}}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));

    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(GEOMETRY), is(true));

    Map<String, Object> geometry = getNestedObject(brewery, GEOMETRY);
    assertThat(geometry.size(), is(3));
    assertThat(geometry.get("type"), is("MULTIPOINT"));
    assertThat(geometry.get("asWKT"), is("MULTIPOINT ((5.979274334569982 52.21715768613606))"));
    assertThat(geometry.get("asWKB"), is("00000000040000000100000000014017eac6e4232933404a1bcbd2b403c4"));
  }

  @Test
  void getRequest_ReturnsBreweryWithAggregateType_forMultipleBeers() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_ReturnsBreweryWithAggregateType_forSingleBeer() {
    String query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c11666\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_ReturnsBreweryWithAggregateType_forNoBeer() {
    String query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c31959\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "totalCount : count( field : \"soldPerYear\" )"
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) "
        + "tastes: stringJoin( field: \"taste\" ) } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));
    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey(BEER_AGG), is(true));
    Map<String, Object> beerAgg = getNestedObject(brewery, BEER_AGG);
    assertThat(beerAgg.size(), is(1));
    assertThat(beerAgg.get("totalCount"), is(0));
  }

  @Test
  void getRequest_ReturnsBeerWithAggregateType_forIngredients() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ totalWeight : floatSum( field : \"weight\" ) "
        + "averageWeight : floatAvg( field : \"weight\" ) maxWeight : floatMax( field : \"weight\" )"
        + "countWeight : count( field : \"weight\", distinct : false )  } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_ReturnsBeerWithAggregateType_forDuplicateAvg() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ avgA : floatAvg( field : \"weight\" ) " + "avgB : floatAvg( field : \"weight\" ) "
        + "avgC : floatAvg( field : \"weight\" )  } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_ReturnsTheIngredientAndTheBeersItIsPartOf_forJoinWithReferencedColumn() {
    String query = "{ingredient(identifier_ingredient: \"cd79545c-5fbb-11eb-ae93-0242ac130002\") {name partOf{name }}}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  @Disabled("see story DHUB-288")
  void getRequest_ReturnsBeerWithAggregateType_forCountDistinct() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ countWeightDis : count( field : \"weight\", distinct : true ) "
        + "countWeightDef : count( field : \"weight\" ) "
        + "countWeight : count( field : \"weight\", distinct : false )  } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_ReturnsBeerWithStringJoinAggregateType_forString() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name taste ingredientAgg{ totalCount : count( field : \"weight\" )"
        + "names : stringJoin( field : \"name\", distinct : false, separator : \"*\" )  } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BEER), is(true));
    Map<String, Object> beer = getNestedObject(data, BEER);
    assertThat(beer.containsKey(INGREDIENT_AGG), is(true));
    Map<String, Object> ingredientAgg = getNestedObject(beer, INGREDIENT_AGG);
    assertThat(ingredientAgg.size(), is(2));
    assertThat(ingredientAgg.get("names"), is("Water*Hop*Barley*Yeast*Orange*Caramel"));
    assertThat(ingredientAgg.get("totalCount"), is(6));
  }

  @Test
  void getRequest_ReturnsBeerWithStringJoinAggregateType_forStringArray() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name beerAgg{ totalCount : count( field : \"soldPerYear\" ) "
        + "tastes : stringJoin( field : \"taste\", distinct : true ) } } }";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_returnsBreweryWithPostalAddressAndUnknownVisitAddress_forBreweryWithoutVisitAddres() {
    String query = "{brewery (identifier_brewery : \"6e8f89da-9676-4cb9-801b-aeb6e2a59ac9\")"
        + "{name beerAgg{ totalCount : count( field : \"soldPerYear\" ) "
        + "tastes : stringJoin( field : \"taste\", distinct : true )} " + "name postalAddress { street city } "
        + " visitAddress {street city} } }";
    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data.size(), is(1));
    assertThat(data.containsKey(BREWERY), is(true));
    Map<String, Object> brewery = getNestedObject(data, BREWERY);
    assertThat(brewery.containsKey("postalAddress"), is(true));
    Map<String, Object> postalAddress = getNestedObject(brewery, "postalAddress");
    assertThat(postalAddress.get("street"), is("5th Avenue"));
    assertThat(postalAddress.get("city"), is("New York"));
    assertThat(brewery.containsKey("visitAddress"), is(false));
    assertThat(brewery.containsKey(BEER_AGG), is(true));
    Map<String, Object> beerAgg = getNestedObject(brewery, BEER_AGG);
    assertThat(beerAgg.size(), is(2));
    assertThat(beerAgg.get("tastes"), is("MEATY,SMOKY,SPICY"));
    assertThat(beerAgg.get("totalCount"), is(2));
  }


  @Test
  void getRequest_returnsBreweries_withStringFilter() {
    String query = "{breweries(filter: {name: {eq: \"Brewery X\"}}){ identifier_brewery name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, equalTo(Map.of("breweries",
        List.of(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")))));
  }

  @Test
  void getRequest_returnsBreweries_withBooleanFilter() {
    String query = "{breweries(filter: {multinational: true}){ identifier_brewery name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo(BREWERIES), Matchers.instanceOf(List.class)));
    assertThat(data.size(), is(1));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(data.containsKey(BREWERIES), is(true));
    assertThat(breweries.get(0),
        is(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")));
  }

  @Test
  void getRequest_returnsBreweries_withBooleanNullFilter() {
    String query = "{breweries(filter: {multinational: null}){ identifier_brewery name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo(BREWERIES), Matchers.instanceOf(List.class)));
    assertThat(data.size(), is(1));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(data.containsKey(BREWERIES), is(true));
    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0),
        is(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X")));
  }

  @Test
  void getRequest_returnsBreweries_withNestedFilter() {
    String query = "{breweries { identifier_brewery name beers(filter: {sinceDate: {gte: \"2016-01-01\"}}) "
        + "{ identifier_beer name} }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo(BREWERIES), Matchers.instanceOf(List.class)));

    List<Map<String, Object>> breweries = getNestedObjects(data, BREWERIES);
    assertThat(data.containsKey(BREWERIES), is(true));
    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0),
        is(Map.of("identifier_brewery", "d3654375-95fa-46b4-8529-08b0f777bd6b", "name", "Brewery X", "beers",
            List.of(Map.of("identifier_beer", "a5148422-be13-452a-b9fa-e72c155df3b2", "name", "Beer 4")))));

    assertThat(breweries.get(1),
        is(Map.of("identifier_brewery", "6e8f89da-9676-4cb9-801b-aeb6e2a59ac9", "name", "Brewery Y", "beers",
            List.of(Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"),
                Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f0ed", "name", "Beer 5")))));

    assertThat(breweries.get(2), is(
        Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c31959", "name", "Brewery Z", "beers", List.of())));

    assertThat(breweries.get(3),
        (is(Map.of("identifier_brewery", "28649f76-ddcf-417a-8c1d-8e5012c11666", "name", "Brewery S", "beers",
            List.of(Map.of("identifier_beer", "766883b5-3482-41cf-a66d-a81e79a4f666", "name", "Beer 6"))))));
  }

  @Test
  void getRequest_returnsBeers_withDateGreaterThenFilter() {
    String query = "{beers(filter: {sinceDate: {gt: \"2016-01-01\"}}){ identifier_beer name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
    String query = "{beers(filter: {sinceDate: {gte: \"2016-01-01\"}}){ identifier_beer name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
    String query = "{beers(filter: {sinceDate: {lt: \"2016-01-01\"}}){ identifier_beer name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(2));

    assertThat(beers, is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
        Map.of("identifier_beer", "1295f4c1-846b-440c-b302-80bbc1f9f3a9", "name", "Beer 2"))));
  }


  @Test
  void getRequest_returnsBeers_withDateLowerThenEqualsFilter() {
    String query = "{beers(filter: {sinceDate: {lte: \"2016-01-01\"}}){ identifier_beer name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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
  void getRequest_returnsBeers_withDateTimeGreaterThenEqualsFilter() {
    String query = "{beers(filter: {lastBrewed: {gte: \"2020-08-11T10:15:30+01:00\"}}){ identifier_beer name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(2));
    assertThat(beers, is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"),
        Map.of("identifier_beer", "973832e7-1dd9-4683-a039-22390b1c1995", "name", "Beer 3"))));
  }

  @Test
  void getRequest_returnsBeers_withMultiOperandFilter() {
    String query =
        "{beers(filter: {lastBrewed: {gte: \"2020-08-11T10:15:30+01:00\", lt: \"2020-09-11T10:15:30+01:00\"}})"
            + "{ identifier_beer name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

    assertThat(data, hasEntry(equalTo(BEERS), Matchers.instanceOf(List.class)));
    assertThat(data.containsKey(BEERS), is(true));

    List<Map<String, Object>> beers = getNestedObjects(data, BEERS);
    assertThat(beers.size(), is(1));
    assertThat(beers, is(List.of(Map.of("identifier_beer", "b0e7cf18-e3ce-439b-a63e-034c8452f59c", "name", "Beer 1"))));
  }

  @Test
  void getRequest_returnsBeers_withNotFilter() {
    String query = "{beers(filter: {lastBrewed: {not: {gte: \"2020-08-11T10:15:30+01:00\"}}}){ identifier_beer name }}";

    JsonNode json = getRequestResult(query);

    assertThat(json.has(ERRORS), is(false));

    Map<String, Object> data = getDataFromJsonNode(json);

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

  private JsonNode getRequestResult(String query) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/")
        .queryParam("query", query);

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
  private List<Map<String, Object>> getNestedObjects(Map<String, Object> data, String name) {
    return (List<Map<String, Object>>) data.get(name);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getNestedObject(Map<String, Object> data, String name) {
    return (Map<String, Object>) data.get(name);
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
