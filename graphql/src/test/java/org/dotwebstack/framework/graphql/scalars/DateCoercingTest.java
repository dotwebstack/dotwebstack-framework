package org.dotwebstack.framework.graphql.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

class DateCoercingTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static DatatypeFactory datatypeFactory;

  private final DateCoercing coercing = new DateCoercing();

  static {
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void serialize_ReturnsDate_ForValidDateString() {
    // Act
    LocalDate date = coercing.serialize("2018-05-30");

    // Assert
    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ThrowsException_ForInvalidDateString() {
    // Act / Assert
    assertThrows(CoercingSerializeException.class, () ->
        coercing.serialize("foo"));
  }

  @Test
  void serialize_ReturnsDate_ForValidLiteral() {
    // Arrange
    Literal dateLiteral = VF.createLiteral(
        datatypeFactory.newXMLGregorianCalendar("2018-05-30"));

    // Act
    LocalDate date = coercing.serialize(dateLiteral);

    // Assert
    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ReturnsDate_ForInvalidLiteral() {
    // Act / Assert
    assertThrows(CoercingSerializeException.class, () ->
        coercing.serialize(VF.createLiteral("foo")));
  }

  @Test
  void serialize_ReturnsDate_ForOtherTypes() {
    // Act / Assert
    assertThrows(CoercingSerializeException.class, () ->
        coercing.serialize(123));
  }

  @Test
  void parseValue_ThrowsException() {
    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        coercing.parseValue(new Object()));
  }

  @Test
  void parseLiteral_ThrowsException() {
    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () ->
        coercing.parseLiteral(new Object()));
  }
}
