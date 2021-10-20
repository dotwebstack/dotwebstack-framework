package org.dotwebstack.framework.integrationtest.rmlpostgres;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.test.TestApplication;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
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

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@Disabled
class RmlPostgresIntegrationTest {

  @Autowired
  private WebTestClient client;

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
    registry.add("dotwebstack.postgres.host", () -> postgreSqlContainer.getHost());
    registry.add("dotwebstack.postgres.port", () -> postgreSqlContainer.getFirstMappedPort());
    registry.add("dotwebstack.postgres.username", () -> postgreSqlContainer.getUsername());
    registry.add("dotwebstack.postgres.password", () -> postgreSqlContainer.getPassword());
    registry.add("dotwebstack.postgres.database", () -> postgreSqlContainer.getDatabaseName());
  }

  @ParameterizedTest
  @MethodSource("createMediaTypeResponseFileNameArguments")
  void dereference_returnsExpectedResult_forRequest(MediaType mediaType, String expectedResultFileName)
      throws IOException {
    RDFFormat rdfFormat = Rio.getParserFormatForFileName(expectedResultFileName)
        .orElseThrow(
            () -> illegalArgumentException("could not determine rdf format from filename: {}", expectedResultFileName));
    Model expected = Rio.parse(getFileInputStream(expectedResultFileName), rdfFormat);

    String responseBody = client.get()
        .uri("/id/beer/b0e7cf18-e3ce-439b-a63e-034c8452f59c")
        .accept(mediaType)
        .exchange()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();

    Model result = Rio.parse(IOUtils.toInputStream(responseBody, StandardCharsets.UTF_8), rdfFormat);
    assertThat(result, is(expected));
  }

  private static Stream<Arguments> createMediaTypeResponseFileNameArguments() {
    return Stream.of(Arguments.of(MediaType.parseMediaType("application/ld+json"), "beer.jsonld"),
        Arguments.of(MediaType.parseMediaType("text/n3"), "beer.n3"),
        Arguments.of(MediaType.parseMediaType("application/n-quads"), "beer.nq"),
        Arguments.of(MediaType.parseMediaType("application/n-triples"), "beer.nt"),
        Arguments.of(MediaType.parseMediaType("application/rdf+xml"), "beer.xml"),
        Arguments.of(MediaType.parseMediaType("application/trig"), "beer.trig"),
        Arguments.of(MediaType.parseMediaType("text/turtle"), "beer.ttl"));
  }

  private static InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("results")
        .resolve(filename));
  }
}
