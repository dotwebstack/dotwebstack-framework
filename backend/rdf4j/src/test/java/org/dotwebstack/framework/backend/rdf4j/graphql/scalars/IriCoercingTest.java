package org.dotwebstack.framework.backend.rdf4j.graphql.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import org.dotwebstack.framework.backend.rdf4j.Constants;
import org.eclipse.rdf4j.model.IRI;
import org.junit.jupiter.api.Test;

class IriCoercingTest {

  private final IriCoercing coercing = new IriCoercing();

  @Test
  void serialize_ThrowsException() {
    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        coercing.serialize(new Object()));
  }

  @Test
  void parseValue_ThrowsException() {
    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        coercing.parseValue(new Object()));
  }

  @Test
  void parseLiteral_ReturnsIri_ForValidStringValue() {
    // Arrange
    StringValue stringValue = StringValue
        .newStringValue(Constants.BUILDING_SHAPE.stringValue())
        .build();

    // Act
    IRI result = coercing.parseLiteral(stringValue);

    // Assert
    assertThat(result, is(equalTo(Constants.BUILDING_SHAPE)));
  }


  @Test
  void parseValue_ThrowsException_ForInvalidStringValue() {
    // Arrange
    StringValue stringValue = StringValue
        .newStringValue("foo")
        .build();

    // Act / Assert
    assertThrows(CoercingParseLiteralException.class, () ->
        coercing.parseLiteral(stringValue));
  }

  @Test
  void parseValue_ThrowsException_ForInvalidType() {
    // Act / Assert
    assertThrows(CoercingParseLiteralException.class, () ->
        coercing.parseLiteral(new Object()));
  }

}
