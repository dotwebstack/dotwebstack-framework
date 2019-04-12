package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.backend.rdf4j.Constants.BUILDING_CLASS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.rdf4j.model.IRI;
import org.junit.jupiter.api.Test;

class IriConverterTest {

  private final IriConverter iriConverter = new IriConverter();

  @Test
  void convert_ConvertsToIri_ForValidString() {
    // Arrange
    String input = BUILDING_CLASS.stringValue();

    // Act
    IRI result = iriConverter.convert(input);

    // Assert
    assertThat(result, is(equalTo(BUILDING_CLASS)));
  }

  @Test
  void convert_ThrowsException_ForInvalidString() {
    // Arrange
    String input = "foo";

    // Act / Assert
    assertThrows(IllegalArgumentException.class, () ->
        iriConverter.convert(input));
  }

}
