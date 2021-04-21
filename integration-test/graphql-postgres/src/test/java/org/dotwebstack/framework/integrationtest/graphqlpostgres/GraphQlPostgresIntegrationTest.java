package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.r2dbc.spi.ConnectionFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.test.TestApplication;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIn;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;

@SuppressWarnings("unchecked")
@SpringBootTest(classes = TestApplication.class)
@Testcontainers
class GraphQlPostgresIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  @Container
  static TestPostgreSqlContainer postgreSqlContainer = new TestPostgreSqlContainer();

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
  }

  @org.springframework.boot.test.context.TestConfiguration
  static class TestConfiguration {
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
      ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
      initializer.setConnectionFactory(connectionFactory);

      CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
      populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("config/model/schema.sql")));
      populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("config/model/data.sql")));
      initializer.setDatabasePopulator(populator);

      return initializer;
    }
  }

  @Test
  void graphQlQuery_ReturnsBeers_Default() {
    String query = "{beers{identifier_beer name}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beers"));

    List<Map<String, Object>> beers = ((List<Map<String, Object>>) data.get("beers"));
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get("name"), is("Beer 1"));
  }

  @Test
  void graphQlQuery_ReturnsPublisher_forBeerSubscription() {
    String query = "subscription {beersSubscription{identifier_beer name}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());

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
  void graphQlQuery_ReturnsBeer_forIdentifier() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){name}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beer"));

    Map<String, Object> beer = ((Map<String, Object>) data.get("beer"));
    assertThat(beer.get("name"), is("Beer 1"));
  }

  @Test
  void graphQlQuery_ReturnsBeerWithNestedObject_forIdentifier() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){ name brewery { name }}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beer"));

    Map<String, Object> beer = ((Map<String, Object>) data.get("beer"));
    assertThat(beer.get("name"), is("Beer 1"));
    assertTrue(beer.containsKey("brewery"));

    Map<String, Object> brewery = (Map<String, Object>) beer.get("brewery");
    assertThat(brewery.get("name"), is("Brewery X"));
  }

  @Test
  void graphQlQuery_ReturnsBreweries_Default() {
    String query = "{breweries{name status}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("breweries"));

    List<Map<String, Object>> breweries = ((List<Map<String, Object>>) data.get("breweries"));
    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0)
        .get("name"), is("Brewery X"));
    assertThat(breweries.get(1)
        .get("status"), is("active"));
  }

  @Test
  void graphQlQuery_returnsBreweriesrWithMappedBy_default() {
    String query = "{breweries{name status beers{name}}}";

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    ExecutionResult result = graphQL.execute(executionInput);

    assertTrue(result.getErrors()
        .isEmpty());

    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("breweries"));

    List<Map<String, Object>> breweries = ((List<Map<String, Object>>) data.get("breweries"));
    assertThat(breweries.size(), is(4));
    assertThat(breweries.get(0)
        .get("name"), is("Brewery X"));
    assertThat(breweries.get(1)
        .get("status"), is("active"));

    List<Map<String, Object>> beers = ((List<Map<String, Object>>) breweries.get(0)
        .get("beers"));
    assertThat(beers.size(), is(3));

    assertThat(beers.stream()
        .map(map -> map.get("name"))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 1", "Beer 2", "Beer 4")));

    beers = ((List<Map<String, Object>>) breweries.get(1)
        .get("beers"));
    assertThat(beers.size(), is(2));

    assertThat(beers.stream()
        .map(map -> map.get("name"))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 3", "Beer 5")));
  }

  @Test
  void graphQlQuery_returnsBreweryWithMappedBy_forIdentifier() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name status beers{name}}}";

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    ExecutionResult result = graphQL.execute(executionInput);

    assertTrue(result.getErrors()
        .isEmpty());

    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));

    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertThat(brewery.size(), is(3));
    assertThat(brewery.get("name"), is("Brewery X"));
    assertThat(brewery.get("status"), is("active"));

    List<Map<String, Object>> beers = ((List<Map<String, Object>>) brewery.get("beers"));
    assertThat(beers.size(), is(3));

    assertThat(beers.stream()
        .map(map -> map.get("name"))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Beer 1", "Beer 2", "Beer 4")));
  }

  @Test
  void graphQlQuery_returnsBeersWithIngredients_forQueryWithJoinTable() {
    String query = "{beers{name ingredients{name}}}";

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    ExecutionResult result = graphQL.execute(executionInput);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beers"));

    List<Map<String, Object>> beers = ((List<Map<String, Object>>) data.get("beers"));
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get("name"), is("Beer 1"));

    List<Map<String, Object>> ingredients = ((List<Map<String, Object>>) beers.get(0)
        .get("ingredients"));
    assertThat(ingredients.size(), is(6));

    assertThat(ingredients.stream()
        .map(map -> map.get("name"))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Water", "Hop", "Barley", "Yeast", "Orange", "Caramel")));

    ingredients = ((List<Map<String, Object>>) beers.get(3)
        .get("ingredients"));
    assertThat(ingredients.size(), is(4));

    assertThat(ingredients.stream()
        .map(map -> map.get("name"))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Water", "Hop", "Barley", "Yeast")));
  }

  @Test
  void graphQlQuery_returnsBeersWithIngredient_forQueryWithJoinTable() {
    String query = "{beers{name ingredient{name}}}";

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    ExecutionResult result = graphQL.execute(executionInput);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beers"));

    List<Map<String, Object>> beers = ((List<Map<String, Object>>) data.get("beers"));
    assertThat(beers.size(), is(6));

    // assertion beer 4
    assertThat(beers.get(3)
        .get("name"), is("Beer 4"));
    Map<String, Object> ingredientBeer4 = ((Map<String, Object>) beers.get(3)
        .get("ingredient"));
    assertThat(ingredientBeer4.size(), is(1));
    assertThat(ingredientBeer4.get("name"), is(IsIn.oneOf("Water", "Hop", "Barley", "Yeast")));

    // assertions beer 6
    assertThat(beers.get(5)
        .get("name"), is("Beer 6"));

    Map<String, Object> ingredientBeer5 = ((Map<String, Object>) beers.get(5)
        .get("ingredient"));
    assertThat(ingredientBeer5.size(), is(1));
    assertThat(ingredientBeer5.get("name"), equalTo("Water"));
  }

  @Test
  void graphQlQuery_returnsBeersWithDeepNesting_default() {
    String query = "{beers{identifier_beer name brewery{name beers{name ingredients{name}}}}}";

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    ExecutionResult result = graphQL.execute(executionInput);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beers"));

    List<Map<String, Object>> beers = ((List<Map<String, Object>>) data.get("beers"));
    assertThat(beers.size(), is(6));
    assertThat(beers.get(0)
        .get("name"), is("Beer 1"));

    Map<String, Object> brewery = ((Map<String, Object>) beers.get(0)
        .get("brewery"));
    assertThat(brewery.size(), is(2));
    assertThat(brewery.get("name"), is("Brewery X"));

    beers = ((List<Map<String, Object>>) brewery.get("beers"));
    assertThat(beers.size(), is(3));
    assertThat(beers.get(1)
        .get("name"), is("Beer 2"));

    List<Map<String, Object>> ingredients = ((List<Map<String, Object>>) beers.get(1)
        .get("ingredients"));
    assertThat(ingredients.size(), is(5));

    assertThat(ingredients.stream()
        .map(map -> map.get("name"))
        .map(Objects::toString)
        .collect(Collectors.toList()), equalTo(List.of("Water", "Hop", "Barley", "Yeast", "Orange")));
  }

  @Test
  void graphQlQuery_ReturnsBreweryWithNestedGeometry_forIdentifier() {
    String query =
        "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name geometry{type asWKT asWKB}}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));

    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertTrue(brewery.containsKey("geometry"));

    Map<String, Object> geometry = (Map<String, Object>) brewery.get("geometry");
    assertThat(geometry.size(), is(3));
    assertThat(geometry.get("type"), is("POINT"));
    assertThat(geometry.get("asWKT"), is("POINT (5.979274334569982 52.21715768613606)"));
    assertThat(geometry.get("asWKB"), is("00000000014017eac6e4232933404a1bcbd2b403c4"));
  }

  @Test
  void graphQlQuery_ReturnsBreweryWithGeometryType_forGeometryType() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name geometry(type : MULTIPOINT){type asWKT asWKB}}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));

    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertTrue(brewery.containsKey("geometry"));

    Map<String, Object> geometry = (Map<String, Object>) brewery.get("geometry");
    assertThat(geometry.size(), is(3));
    assertThat(geometry.get("type"), is("MULTIPOINT"));
    assertThat(geometry.get("asWKT"), is("MULTIPOINT ((5.979274334569982 52.21715768613606))"));
    assertThat(geometry.get("asWKB"), is("00000000040000000100000000014017eac6e4232933404a1bcbd2b403c4"));
  }

  @Test
  void graphQlQuery_ReturnsBreweryWithAggregateType_forMultipleBeers() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));
    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertTrue(brewery.containsKey("beerAgg"));
    Map<String, Object> beerAgg = ((Map<String, Object>) brewery.get("beerAgg"));
    assertThat(beerAgg.size(), is(3));
    assertThat(beerAgg.get("totalSold"), is(1700000));
    assertThat(beerAgg.get("averageSold"), is(566667));
    assertThat(beerAgg.get("maxSold"), is(1000000));
  }

  @Test
  void graphQlQuery_ReturnsBreweryWithAggregateType_forSingleBeer() {
    String query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c11666\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));
    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertTrue(brewery.containsKey("beerAgg"));
    Map<String, Object> beerAgg = ((Map<String, Object>) brewery.get("beerAgg"));
    assertThat(beerAgg.size(), is(3));
    assertThat(beerAgg.get("totalSold"), is(50000));
    assertThat(beerAgg.get("averageSold"), is(50000));
    assertThat(beerAgg.get("maxSold"), is(50000));
  }

  @Test
  void graphQlQuery_ReturnsBreweryWithAggregateType_forNoBeer() {
    String query = "{brewery (identifier_brewery : \"28649f76-ddcf-417a-8c1d-8e5012c31959\")"
        + "{name beerAgg{ totalSold : intSum( field : \"soldPerYear\" ) "
        + "totalCount : count( field : \"soldPerYear\" )"
        + "averageSold : intAvg( field : \"soldPerYear\" ) maxSold : intMax( field : \"soldPerYear\" ) "
        + "tastes: stringJoin( field: \"taste\" ) } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));
    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertTrue(brewery.containsKey("beerAgg"));
    Map<String, Object> beerAgg = ((Map<String, Object>) brewery.get("beerAgg"));
    assertThat(beerAgg.size(), is(5));
    assertThat(beerAgg.get("totalSold"), is(nullValue()));
    assertThat(beerAgg.get("totalCount"), is(0));
    assertThat(beerAgg.get("averageSold"), is(nullValue()));
    assertThat(beerAgg.get("maxSold"), is(nullValue()));
    assertThat(beerAgg.get("tastes"), is(nullValue()));
  }

  @Test
  void graphQlQuery_ReturnsBeerWithAggregateType_forIngredients() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ totalWeight : floatSum( field : \"weight\" ) "
        + "averageWeight : floatAvg( field : \"weight\" ) maxWeight : floatMax( field : \"weight\" )"
        + "countWeight : count( field : \"weight\", distinct : false )  } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beer"));
    Map<String, Object> beer = ((Map<String, Object>) data.get("beer"));
    assertTrue(beer.containsKey("ingredientAgg"));
    Map<String, Object> ingredientAgg = ((Map<String, Object>) beer.get("ingredientAgg"));
    assertThat(ingredientAgg.size(), is(4));
    assertThat(ingredientAgg.get("totalWeight"), is(22.2));
    assertThat(ingredientAgg.get("averageWeight"), is(3.7));
    assertThat(ingredientAgg.get("maxWeight"), is(6.6));
    assertThat(ingredientAgg.get("countWeight"), is(6));
  }

  @Test
  void graphQlQuery_ReturnsBeerWithAggregateType_forDuplicateAvg() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ avgA : floatAvg( field : \"weight\" ) " + "avgB : floatAvg( field : \"weight\" ) "
        + "avgC : floatAvg( field : \"weight\" )  } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beer"));
    Map<String, Object> beer = ((Map<String, Object>) data.get("beer"));
    assertTrue(beer.containsKey("ingredientAgg"));
    Map<String, Object> ingredientAgg = ((Map<String, Object>) beer.get("ingredientAgg"));
    assertThat(ingredientAgg.size(), is(3));
    assertThat(ingredientAgg.get("avgA"), is(3.7));
    assertThat(ingredientAgg.get("avgB"), is(3.7));
    assertThat(ingredientAgg.get("avgC"), is(3.7));
  }

  @Test
  public void graphQlQuery_ReturnsTheIngredientAndTheBeersItIsPartOf_forJoinWithReferencedColumn() {
    String query = "{ingredient(identifier_ingredient: \"cd79545c-5fbb-11eb-ae93-0242ac130002\") {name partOf{name }}}";
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();
    ExecutionResult result = graphQL.execute(executionInput);
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("ingredient"));
    Map<String, Object> ingredient = ((Map<String, Object>) data.get("ingredient"));
    assertThat(ingredient.get("name"), is("Caramel"));
    List<Map<String, Object>> beers = (List<Map<String, Object>>) ingredient.get("partOf");
    assertThat(beers.size(), is(2));
    Map<String, Object> beer1 = (Map<String, Object>) beers.get(0);
    assertThat(beer1.get("name"), is("Beer 1"));
    Map<String, Object> beer3 = (Map<String, Object>) beers.get(1);
    assertThat(beer3.get("name"), is("Beer 3"));



  }

  @Test
  @Disabled("see story DHUB-288")
  void graphQlQuery_ReturnsBeerWithAggregateType_forCountDistinct() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name ingredientAgg{ countWeightDis : count( field : \"weight\", distinct : true ) "
        + "countWeightDef : count( field : \"weight\" ) "
        + "countWeight : count( field : \"weight\", distinct : false )  } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beer"));
    Map<String, Object> beer = ((Map<String, Object>) data.get("beer"));
    assertTrue(beer.containsKey("ingredientAgg"));
    Map<String, Object> ingredientAgg = ((Map<String, Object>) beer.get("ingredientAgg"));
    assertThat(ingredientAgg.size(), is(3));
    assertThat(ingredientAgg.get("countWeightDis"), is(5));
    assertThat(ingredientAgg.get("countWeightDef"), is(6));
    assertThat(ingredientAgg.get("countWeight"), is(6));
  }

  @Test
  void graphQlQuery_ReturnsBeerWithStringJoinAggregateType_forString() {
    String query = "{beer(identifier_beer : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\")"
        + "{name taste ingredientAgg{ totalCount : count( field : \"weight\" )"
        + "names : stringJoin( field : \"name\", distinct : false, separator : \"*\" )  } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beer"));
    Map<String, Object> beer = ((Map<String, Object>) data.get("beer"));
    assertTrue(beer.containsKey("ingredientAgg"));
    Map<String, Object> ingredientAgg = ((Map<String, Object>) beer.get("ingredientAgg"));
    assertThat(ingredientAgg.size(), is(2));
    assertThat(ingredientAgg.get("names"), is("Water*Hop*Barley*Yeast*Orange*Caramel"));
    assertThat(ingredientAgg.get("totalCount"), is(6));
  }

  @Test
  void graphQlQuery_ReturnsBeerWithStringJoinAggregateType_forStringArray() {
    String query = "{brewery (identifier_brewery : \"d3654375-95fa-46b4-8529-08b0f777bd6b\")"
        + "{name beerAgg{ totalCount : count( field : \"soldPerYear\" ) "
        + "tastes : stringJoin( field : \"taste\", distinct : true ) } } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));
    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertTrue(brewery.containsKey("beerAgg"));
    Map<String, Object> beerAgg = ((Map<String, Object>) brewery.get("beerAgg"));
    assertThat(beerAgg.size(), is(2));
    assertThat(beerAgg.get("tastes"), is("fruity,meaty,smoky,watery"));
    assertThat(beerAgg.get("totalCount"), is(3));
  }

  @Test
  void graphQlQuery_returnsBreweryWithPostalAddressAndUnknownVisitAddress_forBreweryWithoutVisitAddres() {
    String query = "{brewery (identifier_brewery : \"6e8f89da-9676-4cb9-801b-aeb6e2a59ac9\")"
        + "{name beerAgg{ totalCount : count( field : \"soldPerYear\" ) "
        + "tastes : stringJoin( field : \"taste\", distinct : true )} " + "name postalAddress { street city } "
        + " visitAddress {street city} } }";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();
    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("brewery"));
    Map<String, Object> brewery = ((Map<String, Object>) data.get("brewery"));
    assertTrue(brewery.containsKey("postalAddress"));
    Map<String, Object> postalAddress = ((Map<String, Object>) brewery.get("postalAddress"));
    assertThat(postalAddress.get("street"), is("5th Avenue"));
    assertThat(postalAddress.get("city"), is("New York"));
    assertTrue(brewery.containsKey("visitAddress"));
    assertThat(brewery.get("visitAddress"), nullValue());
    assertTrue(brewery.containsKey("beerAgg"));
    Map<String, Object> beerAgg = ((Map<String, Object>) brewery.get("beerAgg"));
    assertThat(beerAgg.size(), is(2));
    assertThat(beerAgg.get("tastes"), is("meaty,smoky,spicy"));
    assertThat(beerAgg.get("totalCount"), is(2));

  }
}
