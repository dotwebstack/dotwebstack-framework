package org.dotwebstack.framework.core.graphql.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.CoercingSerializeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

class DateTimeCoercingTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static DatatypeFactory datatypeFactory;

  private final DateTimeCoercing coercing = new DateTimeCoercing();

  static {
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void serialize_ReturnsDate_ForValidDateTimeString() {
    // Act
    ZonedDateTime dateTime = coercing.serialize("2018-05-30T09:30:10+02:00");

    // Assert
    ZonedDateTime expected = ZonedDateTime.of(2018, 5, 30, 9, 30, 10, 0, ZoneId.of("GMT+2"));
    assertThat(dateTime.isEqual(expected), is(equalTo(true)));
  }

  @Test
  void serialize_ThrowsException_ForInvalidDateTimeString() {
    // Act / Assert
    assertThrows(CoercingSerializeException.class, () ->
        coercing.serialize("foo"));
  }

  @Test
  void serialize_ReturnsDate_ForValidLiteral() {
    // Arrange
    Literal dateTimeLiteral = VF.createLiteral(
        datatypeFactory.newXMLGregorianCalendar("2018-05-30T09:30:10+02:00"));

    // Act
    ZonedDateTime dateTime = coercing.serialize(dateTimeLiteral);

    // Assert
    ZonedDateTime expected = ZonedDateTime.of(2018, 5, 30, 9, 30, 10, 0, ZoneId.of("GMT+2"));
    assertThat(dateTime.isEqual(expected), is(equalTo(true)));
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
