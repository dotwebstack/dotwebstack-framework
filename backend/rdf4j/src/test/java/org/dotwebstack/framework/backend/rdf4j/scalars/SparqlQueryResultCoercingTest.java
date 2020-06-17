package org.dotwebstack.framework.backend.rdf4j.scalars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.CoercingParseValueException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlQueryResult;
import org.junit.jupiter.api.Test;

class SparqlQueryResultCoercingTest {

  private final SparqlQueryResultCoercing coercing = new SparqlQueryResultCoercing();

  @Test
  void serialize_ThrowsException() {
    // Act / Assert
    assertThrows(IllegalArgumentException.class, () -> coercing.serialize(new Object()));
  }

  @Test
  void parseValue_ThrowsException_whenNotSparqlQueryResult() {
    // Act / Assert
    assertThrows(CoercingParseValueException.class, () -> coercing.parseValue(new Object()));
  }

  @Test
  void parseValue_ReturnsString_ForSparqlQueryResultValue() throws IOException {
    // Arrange
    SparqlQueryResult sparqlQueryResult = getSparqlQueryResultExample();

    // Act
    SparqlQueryResult result = coercing.parseValue(sparqlQueryResult);

    // Assert
    assertThat(new String(result.getInputStream()
        .readAllBytes()), is(new String(getSparqlExampleInputStream().readAllBytes())));
  }

  @Test
  void parseLiteral_ReturnsString_ForSparqlQueryResultLiteral() throws IOException {
    // Arrange
    SparqlQueryResult sparqlQueryResult = getSparqlQueryResultExample();

    // Act
    SparqlQueryResult result = coercing.parseLiteral(sparqlQueryResult);

    // Assert
    assertThat(new String(result.getInputStream()
        .readAllBytes()), is(new String(getSparqlExampleInputStream().readAllBytes())));
  }

  private SparqlQueryResult getSparqlQueryResultExample() throws IOException {
    InputStream is = getSparqlExampleInputStream();
    return new SparqlQueryResult(is);
  }

  private InputStream getSparqlExampleInputStream() throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("test-files")
        .resolve("input-response-mapper.sparql"));
  }
}
