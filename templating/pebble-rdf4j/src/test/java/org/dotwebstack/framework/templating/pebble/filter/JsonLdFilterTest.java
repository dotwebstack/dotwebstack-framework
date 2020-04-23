package org.dotwebstack.framework.templating.pebble.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonLdFilterTest {

  private JsonLdFilter jsonLdFilter;

  @BeforeEach
  void setup() {
    // Arrange
    this.jsonLdFilter = new JsonLdFilter();
  }

  @Test
  public void applyModelTest() throws IOException {
    // Act
    String result = (String) jsonLdFilter.apply(buildModel(), null, null, null, 0);

    // Assert
    assertThat(result, is(new String(getFileInputStream("jsonLdSerialized.txt").readAllBytes())));
  }

  @Test
  public void argumentNamesShouldReturnEmptyList() {
    // Act
    List<String> arguments = jsonLdFilter.getArgumentNames();

    // Assert
    assertThat(arguments.isEmpty(), is(true));
  }

  private Model buildModel() {
    ModelBuilder builder = new ModelBuilder();

    builder.setNamespace("ex", "http://example.org/")
        .setNamespace(FOAF.NS);
    builder.namedGraph("ex:graph1")

        .subject("ex:john")
        .add(FOAF.NAME, "John")
        .add(FOAF.AGE, 42)
        .add(FOAF.MBOX, "john@example.org");

    builder.defaultGraph()
        .subject("ex:graph1")
        .add(RDF.TYPE, "ex:Graph");
    return builder.build();
  }

  private InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("results")
        .resolve(filename));
  }
}
