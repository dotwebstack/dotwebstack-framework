package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
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
import org.junit.jupiter.api.Test;
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
    String query = "{beers{identifier name}}";

    ExecutionResult result = graphQL.execute(query);

    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("beers"));

    List<Map<String, Object>> beers = ((List<Map<String, Object>>) data.get("beers"));
    assertThat(beers.size(), is(5));
    assertThat(beers.get(0)
        .get("name"), is("Beer 1"));
  }

  @Test
  void graphQlQuery_ReturnsBeer_forIdentifier() {
    String query = "{beer(identifier : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){name}}";

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
    String query = "{beer(identifier : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){ name brewery { name }}}";

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
    assertThat(breweries.size(), is(3));
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
    assertThat(breweries.size(), is(3));
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
    String query = "{brewery (identifier : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name status beers{name}}}";

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
  void graphQlQuery_returnsBeersJoinTable_default() {
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
    assertThat(beers.size(), is(5));
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
  void graphQlQuery_returnsBeersWithDeepNesting_default() {
    String query = "{beers{identifier name brewery{name beers{name ingredients{name}}}}}";

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
    assertThat(beers.size(), is(5));
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
    String query = "{brewery (identifier : \"d3654375-95fa-46b4-8529-08b0f777bd6b\"){name geometry{type asWKT asWKB}}}";

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
}
