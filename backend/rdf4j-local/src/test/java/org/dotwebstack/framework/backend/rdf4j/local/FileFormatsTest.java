package org.dotwebstack.framework.backend.rdf4j.local;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

class FileFormatsTest {

  @Test
  void getFormat_ReturnsFileFormat_ForLowerCaseExtension() {
    // Act
    RDFFormat format = FileFormats.getFormat("trig");

    // Assert
    assertThat(format, is(equalTo(RDFFormat.TRIG)));
  }

  @Test
  void getFormat_ReturnsFileFormat_ForMixedCaseExtension() {
    // Act
    RDFFormat format = FileFormats.getFormat("TriG");

    // Assert
    assertThat(format, is(equalTo(RDFFormat.TRIG)));
  }

  @Test
  void getFormat_ThrowsException_ForUnknownExtension() {
    // Act
    RDFFormat format = FileFormats.getFormat("doc");

    // Assert
    assertThat(format, is(equalTo(null)));
  }

}
