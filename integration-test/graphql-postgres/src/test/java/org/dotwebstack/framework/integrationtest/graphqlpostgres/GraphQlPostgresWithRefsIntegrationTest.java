package org.dotwebstack.framework.integrationtest.graphqlpostgres;

import static org.dotwebstack.framework.integrationtest.graphqlpostgres.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.equalToObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.IsIterableContaining.hasItems;

import com.fasterxml.jackson.databind.ObjectMapper;
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

  private final ObjectMapper mapper = new ObjectMapper();

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
}
