package org.dotwebstack.framework.backend.rdf4j.scalars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.CoercingParseValueException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.ParseErrorLogger;
import org.junit.jupiter.api.Test;

class ModelCoercingTest {

  private final ModelCoercing coercing = new ModelCoercing();

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Test
  void serialize_ThrowsException() {
    // Act / Assert
    assertThrows(IllegalArgumentException.class, () -> coercing.serialize(new Object()));
  }

  @Test
  void parseValue_ThrowsException_whenNotModel() {
    // Act / Assert
    assertThrows(CoercingParseValueException.class, () -> coercing.parseValue(new Object()));
  }

  @Test
  void parseValue_ReturnsString_ForModelValue() throws IOException {
    // Arrange
    Model model = getDataTrigExampleModel();

    // Act
    Model result = coercing.parseValue(model);

    // Assert
    assertModelHasResults(result);
  }

  @Test
  void parseLiteral_ReturnsString_ForModelValue() throws IOException {
    // Arrange
    Model model = getDataTrigExampleModel();

    // Act
    Model result = coercing.parseLiteral(model);

    // Assert
    assertModelHasResults(result);
  }

  private void assertModelHasResults(Model result) {
    IRI subject1 = VF.createIRI("https://github.com/dotwebstack/beer/id/brewery/123");

    IRI predicate = VF.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    IRI object = VF.createIRI("https://github.com/dotwebstack/beer/def#Brewery");

    assertThat(result.contains(subject1, predicate, object), is(true));

    IRI subject2 = VF.createIRI("https://github.com/dotwebstack/beer/id/brewery/456");

    assertThat(result.contains(subject2, predicate, object), is(true));
  }


  private Model getDataTrigExampleModel() throws IOException {
    InputStream is = Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("config/model")
        .resolve("data.trig"));
    ParserConfig config = new ParserConfig();
    config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
    return Rio.parse(is, "", RDFFormat.TRIG, config, SimpleValueFactory.getInstance(), new ParseErrorLogger());

  }
}
