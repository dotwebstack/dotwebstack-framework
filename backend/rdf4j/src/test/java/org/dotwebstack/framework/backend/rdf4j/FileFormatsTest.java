package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class FileFormatsTest {

  @TestFactory
  Stream<DynamicTest> getFormat() {
    return Stream.of(getTestCase("Returns RDF Trig for lowercase extension", "trig", RDFFormat.TRIG),
        getTestCase("Returns RDF NQuads for lowercase extension", "nq", RDFFormat.NQUADS),
        getTestCase("Returns RDF Trig for mixed case extension", "TriG", RDFFormat.TRIG),
        getTestCase("Returns RDF NQuads for mixed case extension", "Nq", RDFFormat.NQUADS),
        getTestCase("Returns RDF Trig for uppercase extension", "TRIG", RDFFormat.TRIG),
        getTestCase("Returns RDF NQuads for uppercase extension", "NQ", RDFFormat.NQUADS),
        getTestCase("Returns null for unknown extension (.doc)", "doc", null),
        getTestCase("Returns null for unknown extension (.ttl)", "ttl", null));
  }

  private DynamicTest getTestCase(String name, String extension, RDFFormat expected) {
    return DynamicTest.dynamicTest(name, () -> {
      // Act
      RDFFormat format = FileFormats.getFormat(extension);

      // Assert
      assertThat(format, is(equalTo(expected)));
    });
  }

}
