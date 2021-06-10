package org.dotwebstack.framework.core.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.StringValue;
import graphql.schema.CoercingSerializeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class DateTimeCoercingTest {

  private final DateTimeCoercing coercing = new DateTimeCoercing();

  @Test
  void serialize_ReturnsDateTime_ForDateTime() {
    ZonedDateTime input = ZonedDateTime.now();

    ZonedDateTime dateTime = coercing.serialize(input);

    assertThat(dateTime, is(sameInstance(input)));
  }

  @Test
  void serialize_ReturnsDateTime_ForValidDateTimeString() {
    ZonedDateTime dateTime = coercing.serialize("2018-05-30T09:30:10+02:00");

    ZonedDateTime expected = ZonedDateTime.of(2018, 5, 30, 9, 30, 10, 0, ZoneId.of("GMT+2"));
    assertThat(dateTime.isEqual(expected), is(equalTo(true)));
  }

  @Test
  void serialize_ThrowsException_ForInvalidDateTimeString() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize("foo"));
  }

  @Test
  void serialize_ThrowsException_ForNull() {
    assertThrows(NullPointerException.class, () -> coercing.serialize(null));
  }

  @Test
  void serialize_ReturnsDate_ForOtherTypes() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize(123));
  }

  @Test
  void parseValue_ThrowsException() {
    var value = new Object();
    assertThrows(CoercingSerializeException.class, () -> coercing.parseValue(value));
  }

  @Test
  void parseValue_ThrowsException_ForNull() {
    assertThrows(NullPointerException.class, () -> coercing.parseValue(null));
  }

  @Test
  void parseLiteral_ThrowsException() {
    var value = new Object();
    assertThrows(UnsupportedOperationException.class, () -> coercing.parseLiteral(value));
  }

  @Test
  void parseLiteral_ThrowsException_ForNull() {
    assertThrows(NullPointerException.class, () -> coercing.parseLiteral(null));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForNowLiteral() {
    ZonedDateTime dateTime = coercing.parseLiteral(new StringValue("NOW"))
        .get();

    assertThat(dateTime.getYear(), equalTo(ZonedDateTime.now()
        .getYear()));
    assertThat(dateTime.getMonth(), equalTo(ZonedDateTime.now()
        .getMonth()));
    assertThat(dateTime.getDayOfMonth(), equalTo(ZonedDateTime.now()
        .getDayOfMonth()));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForOffsetDateTime() {
    ZonedDateTime dateTime = coercing.parseLiteral(new StringValue(ZonedDateTime.now()
        .toString()))
        .get();

    assertThat(dateTime.getYear(), equalTo(ZonedDateTime.now()
        .getYear()));
    assertThat(dateTime.getMonth(), equalTo(ZonedDateTime.now()
        .getMonth()));
    assertThat(dateTime.getDayOfMonth(), equalTo(ZonedDateTime.now()
        .getDayOfMonth()));
  }
}
