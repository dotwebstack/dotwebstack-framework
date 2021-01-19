package org.dotwebstack.framework.integrationtest.graphqlrdf4j;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.r2dbc.spi.ConnectionFactory;
import java.util.List;
import java.util.Map;
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

@SuppressWarnings("unchecked")
@SpringBootTest(classes = TestApplication.class)
@Testcontainers
public class GraphQlPostgresIntegrationTest {

  @Autowired
  private GraphQL graphQL;

  @Container
  static TestPostgreSqlContainer postgreSqlContainer = new TestPostgreSqlContainer();

  private static class TestPostgreSqlContainer extends PostgreSQLContainer<TestPostgreSqlContainer> {
    public TestPostgreSqlContainer() {
      super("postgres:11.10");
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
    // Arrange
    String query = "{beers{identifier name}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
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
    // Arrange
    String query = "{beer(identifier : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){name}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
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
    // Arrange
    String query = "{beer(identifier : \"b0e7cf18-e3ce-439b-a63e-034c8452f59c\"){ name brewery { name }}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
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
    // Arrange
    String query = "{breweries{name}}";

    // Act
    ExecutionResult result = graphQL.execute(query);

    // Assert
    assertTrue(result.getErrors()
        .isEmpty());
    Map<String, Object> data = result.getData();

    assertThat(data.size(), is(1));
    assertTrue(data.containsKey("breweries"));

    List<Map<String, Object>> breweries = ((List<Map<String, Object>>) data.get("breweries"));
    assertThat(breweries.size(), is(3));
    assertThat(breweries.get(0)
        .get("name"), is("Brewery X"));
  }
}