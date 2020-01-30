package org.dotwebstack.framework.backend.rdf4j.mapping;

import static org.dotwebstack.framework.backend.rdf4j.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.ParseErrorLogger;
import org.junit.jupiter.api.Test;

public class Notation3ResponseMapperTest {

  @Test
  void shouldMapModelToN3() throws IOException {
    InputStream is = getFileInputStream("input-response-mapper.trig");
    Model model =
        Rio.parse(is, "", RDFFormat.TRIG, new ParserConfig(), SimpleValueFactory.getInstance(), new ParseErrorLogger());

    String actualResult = new Notation3ResponseMapper().toResponse(model);
    String expectedResult = new String(getFileInputStream("output-response-mapper-n3.txt").readAllBytes());

    assertThat(actualResult, equalToIgnoringLineBreaks(expectedResult));
  }

  private InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("test-files")
        .resolve(filename));
  }
}
