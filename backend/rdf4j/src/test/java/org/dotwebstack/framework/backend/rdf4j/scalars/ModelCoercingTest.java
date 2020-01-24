package org.dotwebstack.framework.backend.rdf4j.scalars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.CoercingParseValueException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.ParseErrorLogger;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;

class ModelCoercingTest {

  private final ModelCoercing coercing = new ModelCoercing();

  @Test
  void serialize_ThrowsException() {
    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () -> coercing.serialize(new Object()));
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
    String result = coercing.parseValue(model);

    // Assert
    assertThat(result, StringContains.containsString("https://github.com/dotwebstack/beer/id/brewery/456"));
  }

  @Test
  void parseLiteral_ReturnsString_ForModelValue() throws IOException {
    // Arrange
    Model model = getDataTrigExampleModel();

    // Act
    String result = coercing.parseLiteral(model);

    // Assert
    assertThat(result, StringContains.containsString("https://github.com/dotwebstack/beer/id/brewery/456"));
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
