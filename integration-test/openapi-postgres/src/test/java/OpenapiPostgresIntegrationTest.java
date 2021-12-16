import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.test.TestApplication;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
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
@AutoConfigureWebTestClient
@Testcontainers
class OpenapiPostgresIntegrationTest {

  @Autowired
  private WebTestClient client;

  private final ObjectMapper mapper = new ObjectMapper();

  @Container
  static final TestPostgreSqlContainer postgreSqlContainer = new TestPostgreSqlContainer()
      .withClasspathResourceMapping("config/model", "/docker-entrypoint-initdb.d", BindMode.READ_ONLY);

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
  void breweries_returnsExpectedResult() throws IOException {
    String result = client.get()
        .uri("/breweries")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    assertResult(result, "breweries.json");
  }

  @Test
  void breweries_returnsExpectedResult_withContext() throws IOException {
    String result = client.get()
        .uri("/breweriesInContext")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    assertResult(result, "breweries_context.json");
  }

  @Test
  void breweries_returnsExpectedResult_withExpanded() throws IOException {
    String result = client.get()
        .uri("/breweries?expand=postalAddress")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    assertResult(result, "breweries_postalAddress.json");
  }

  @Test
  @Disabled
  void breweries_returnsExpectedResult_withFilter() throws IOException {
    String result = client.get()
        .uri("/breweries?name=Brewery X")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    assertResult(result, "breweries_filtered.json");
  }

  @Test
  void breweries_returnsExpectedResult_withKey() throws IOException {
    String result = client.get()
        .uri("/brewery/d3654375-95fa-46b4-8529-08b0f777bd6b")
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    assertResult(result, "brewery_X.json");
  }

  private void assertResult(String result, String jsonResult) throws IOException {
    JsonNode expectedObj = mapper.readTree(getClass().getResourceAsStream("/results/" + jsonResult));
    JsonNode actualObj = mapper.readTree(result);

    assertEquals(expectedObj, actualObj);
  }
}
