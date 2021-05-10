package org.dotwebstack.framework.core.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.StringValue;
import graphql.schema.CoercingSerializeException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DateTimeCoercingTest {

  private final DateTimeCoercing coercing = new DateTimeCoercing();

  @Test
  void serialize_ReturnsDateTime_ForDateTime() {
    OffsetDateTime input = OffsetDateTime.now();

    OffsetDateTime dateTime = coercing.serialize(input);

    assertThat(dateTime, is(sameInstance(input)));
  }

  @Test
  void serialize_ReturnsDateTime_ForValidDateTimeString() {
    OffsetDateTime dateTime = coercing.serialize("2018-05-30T09:30:10+02:00");

    OffsetDateTime expected = OffsetDateTime.of(2018, 5, 30, 7, 30, 10, 0, ZoneOffset.UTC);
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
    assertThrows(CoercingSerializeException.class, () -> coercing.parseValue(new Object()));
  }

  @Test
  void parseValue_ThrowsException_ForNull() {
    assertThrows(NullPointerException.class, () -> coercing.parseValue(null));
  }

  @Test
  void parseLiteral_ThrowsException() {
    assertThrows(UnsupportedOperationException.class, () -> coercing.parseLiteral(new Object()));
  }

  @Test
  void parseLiteral_ThrowsException_ForNull() {
    assertThrows(NullPointerException.class, () -> coercing.parseLiteral(null));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForNowLiteral() {
    OffsetDateTime dateTime = coercing.parseLiteral(new StringValue("NOW"));

    assertThat(dateTime.getYear(), equalTo(OffsetDateTime.now()
        .getYear()));
    assertThat(dateTime.getMonth(), equalTo(OffsetDateTime.now()
        .getMonth()));
    assertThat(dateTime.getDayOfMonth(), equalTo(OffsetDateTime.now()
        .getDayOfMonth()));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForOffsetDateTime() {
    OffsetDateTime dateTime = coercing.parseLiteral(new StringValue(OffsetDateTime.now()
        .toString()));

    assertThat(dateTime.getYear(), equalTo(OffsetDateTime.now()
        .getYear()));
    assertThat(dateTime.getMonth(), equalTo(OffsetDateTime.now()
        .getMonth()));
    assertThat(dateTime.getDayOfMonth(), equalTo(OffsetDateTime.now()
        .getDayOfMonth()));
  }
}
