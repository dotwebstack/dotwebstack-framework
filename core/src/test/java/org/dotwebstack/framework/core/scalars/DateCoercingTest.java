package org.dotwebstack.framework.core.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class DateCoercingTest {

  private static final GraphQLContext DEFAULT_CONTEXT = GraphQLContext.getDefault();

  private static final Locale DEFAULT_LOCALE = Locale.getDefault();

  private static final CoercedVariables EMPTY_VARIABLES = CoercedVariables.emptyVariables();

  private final DateCoercing coercing = new DateCoercing();

  @Test
  void serialize_ReturnsDate_ForLocalDate() {
    LocalDate input = LocalDate.now();

    LocalDate date = coercing.serialize(input, DEFAULT_CONTEXT, DEFAULT_LOCALE);

    assertThat(date, is(sameInstance(input)));
  }

  @Test
  void serialize_ReturnsDate_ForDate() {
    Date input = new Date();

    LocalDate date = coercing.serialize(input, DEFAULT_CONTEXT, DEFAULT_LOCALE);

    assertEquals(input.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate(), date);
  }

  @Test
  void serialize_ReturnsDate_ForValidDateString() { // NOSONAR
    LocalDate date = coercing.serialize("2018-05-30", DEFAULT_CONTEXT, DEFAULT_LOCALE);

    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ReturnsDate_ForLocalDateString() { // NOSONAR
    LocalDate date = coercing.serialize("2018-05-30T00:00:00", DEFAULT_CONTEXT, DEFAULT_LOCALE);

    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ReturnsDate_ForZonedDateString() { // NOSONAR
    LocalDate date = coercing.serialize("2018-05-30T00:00:00Z", DEFAULT_CONTEXT, DEFAULT_LOCALE);

    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void serialize_ThrowsException_ForInvalidDateString() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize("foo", DEFAULT_CONTEXT, DEFAULT_LOCALE));
  }

  @Test
  void serialize_ReturnsDate_ForOtherTypes() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize(123, DEFAULT_CONTEXT, DEFAULT_LOCALE));
  }

  @Test
  void parseValue_ThrowsException() {
    var value = new Object();
    assertThrows(CoercingSerializeException.class, () -> coercing.parseValue(value, DEFAULT_CONTEXT, DEFAULT_LOCALE));
  }

  @Test
  void parseValue_ReturnsDate_ForValidDateString() { // NOSONAR
    LocalDate date = coercing.parseValue("2018-05-30", DEFAULT_CONTEXT, DEFAULT_LOCALE)
        .get();

    assertThat(date, is(equalTo(LocalDate.of(2018, 5, 30))));
  }

  @Test
  void parseLiteral_ThrowsException() {
    var value = new Object();
    assertThrows(UnsupportedOperationException.class, () -> coercing.parseLiteral(value));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForNowLiteral() {
    LocalDate localDate =
        coercing.parseLiteral(new StringValue("NOW"), EMPTY_VARIABLES, DEFAULT_CONTEXT, DEFAULT_LOCALE)
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
        .toString()), EMPTY_VARIABLES, DEFAULT_CONTEXT, DEFAULT_LOCALE)
        .get();

    assertThat(localDate.getYear(), equalTo(LocalDate.now()
        .getYear()));
    assertThat(localDate.getMonth(), equalTo(LocalDate.now()
        .getMonth()));
    assertThat(localDate.getDayOfMonth(), equalTo(LocalDate.now()
        .getDayOfMonth()));
  }
}
