package org.dotwebstack.framework.core.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.CoercingSerializeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class DateTimeCoercingTest {

  private final DateTimeCoercing coercing = new DateTimeCoercing();

  @Test
  void serialize_ReturnsDateTime_ForDateTime() {
    // Arrange
    ZonedDateTime input = ZonedDateTime.now();

    // Act
    ZonedDateTime dateTime = coercing.serialize(input);

    // Assert
    assertThat(dateTime, is(sameInstance(input)));
  }

  @Test
  void serialize_ReturnsDateTime_ForValidDateTimeString() {
    // Act
    ZonedDateTime dateTime = coercing.serialize("2018-05-30T09:30:10+02:00");

    // Assert
    ZonedDateTime expected = ZonedDateTime.of(2018, 5, 30, 9, 30, 10, 0, ZoneId.of("GMT+2"));
    assertThat(dateTime.isEqual(expected), is(equalTo(true)));
  }

  @Test
  void serialize_ThrowsException_ForInvalidDateTimeString() {
    // Act / Assert
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize("foo"));
  }

  @Test
  void serialize_ReturnsDate_ForOtherTypes() {
    // Act / Assert
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize(123));
  }

  @Test
  void parseValue_ThrowsException() {
    // Act / Assert
    assertThrows(CoercingSerializeException.class, () -> coercing.parseValue(new Object()));
  }

  @Test
  void parseLiteral_ThrowsException() {
    // Act / Assert
    assertThrows(UnsupportedOperationException.class, () -> coercing.parseLiteral(new Object()));
  }

}
