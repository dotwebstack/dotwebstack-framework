package org.dotwebstack.framework.core.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.language.StringValue;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.jupiter.api.Test;

class DateCoercingTest {

  private final DateCoercing coercing = new DateCoercing();

  @Test
  void serialize_ReturnsDate_ForLocalDate() {
    LocalDate input = LocalDate.now();

    LocalDate date = coercing.serialize(input);

    assertThat(date, is(sameInstance(input)));
  }

  @Test
  void serialize_ReturnsDate_ForDate() {
    Date input = new Date();

    LocalDate date = coercing.serialize(input);

    assertEquals(input.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate(), date);
  }

  @Test
  void serialize_ReturnsDate_ForValidDateString() { // NOSONAR
    LocalDate date = coercing.serialize("2018-05-30");

    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ReturnsDate_ForLocalDateString() { // NOSONAR
    LocalDate date = coercing.serialize("2018-05-30T00:00:00");

    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ReturnsDate_ForZonedDateString() { // NOSONAR
    LocalDate date = coercing.serialize("2018-05-30T00:00:00Z");

    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ThrowsException_ForInvalidDateString() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize("foo"));
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
  void parseLiteral_ThrowsException() {
    var value = new Object();
    assertThrows(UnsupportedOperationException.class, () -> coercing.parseLiteral(value));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForNowLiteral() {
    LocalDate localDate = coercing.parseLiteral(new StringValue("NOW"))
        .get();

    assertThat(localDate.getYear(), equalTo(LocalDate.now()
        .getYear()));
    assertThat(localDate.getMonth(), equalTo(LocalDate.now()
        .getMonth()));
    assertThat(localDate.getDayOfMonth(), equalTo(LocalDate.now()
        .getDayOfMonth()));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForLocalDateNow() {
    LocalDate localDate = coercing.parseLiteral(new StringValue(LocalDate.now()
        .toString()))
        .get();

    assertThat(localDate.getYear(), equalTo(LocalDate.now()
        .getYear()));
    assertThat(localDate.getMonth(), equalTo(LocalDate.now()
        .getMonth()));
    assertThat(localDate.getDayOfMonth(), equalTo(LocalDate.now()
        .getDayOfMonth()));
  }
}
