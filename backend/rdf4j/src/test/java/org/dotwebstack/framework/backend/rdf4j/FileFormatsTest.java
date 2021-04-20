package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

class FileFormatsTest {

  @Test
  void getFormat_ReturnsFileFormat_ForLowerCaseExtension() {
    RDFFormat format = FileFormats.getFormat("trig");

    assertThat(format, is(equalTo(RDFFormat.TRIG)));
  }

  @Test
  void getFormat_ReturnsFileFormat_ForMixedCaseExtension() {
    RDFFormat format = FileFormats.getFormat("TriG");

    assertThat(format, is(equalTo(RDFFormat.TRIG)));
  }

  @Test
  void getFormat_ReturnsNull_ForUnknownExtension() {
    RDFFormat format = FileFormats.getFormat("doc");

    assertThat(format, is(equalTo(null)));
  }

}
