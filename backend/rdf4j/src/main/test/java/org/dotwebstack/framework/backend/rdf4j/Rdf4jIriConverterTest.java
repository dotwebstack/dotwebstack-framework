package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.rdf4j.model.IRI;
import org.junit.jupiter.api.Test;

class Rdf4jIriConverterTest {

  private final Rdf4jIriConverter rdf4jIriConverter = new Rdf4jIriConverter();

  @Test
  void convert_ConvertsToIri_ForValidString() {
    // Arrange
    String input = Constants.BREWERY_CLASS.stringValue();

    // Act
    IRI result = rdf4jIriConverter.convert(input);

    // Assert
    assertThat(result, is(equalTo(Constants.BREWERY_CLASS)));
  }

  @Test
  void convert_ThrowsException_ForInvalidString() {
    // Arrange
    String input = "foo";

    // Act / Assert
    assertThrows(IllegalArgumentException.class, () -> rdf4jIriConverter.convert(input));
  }

}
