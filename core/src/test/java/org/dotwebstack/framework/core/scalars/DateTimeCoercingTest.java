package org.dotwebstack.framework.core.scalars;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.schema.CoercingSerializeException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class DateTimeCoercingTest {

  private static final GraphQLContext DEFAULT_CONTEXT = GraphQLContext.getDefault();

  private static final Locale DEFAULT_LOCALE = Locale.getDefault();

  private static final CoercedVariables EMPTY_VARIABLES = CoercedVariables.emptyVariables();

  private final DateTimeCoercing coercing = new DateTimeCoercing();

  @Test
  void serialize_ThrowsException_ForDeprecatedSignature() {
    var value = new Object();
    assertThrows(UnsupportedOperationException.class, () -> coercing.serialize(value));
  }

  @Test
  void serialize_ReturnsDateTime_ForDateTime() {
    OffsetDateTime input = OffsetDateTime.now();

    OffsetDateTime dateTime = coercing.serialize(input, DEFAULT_CONTEXT, DEFAULT_LOCALE);

    assertThat(dateTime, is(sameInstance(input)));
  }

  @Test
  void serialize_ReturnsDateTime_ForValidDateTimeString() {
    OffsetDateTime dateTime = coercing.serialize("2018-05-30T09:30:10+02:00", DEFAULT_CONTEXT, DEFAULT_LOCALE);

    OffsetDateTime expected = OffsetDateTime.of(2018, 5, 30, 7, 30, 10, 0, ZoneOffset.UTC);
    assertThat(dateTime.isEqual(expected), is(equalTo(true)));
  }

  @Test
  void serialize_ThrowsException_ForInvalidDateTimeString() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize("foo", DEFAULT_CONTEXT, DEFAULT_LOCALE));
  }

  @Test
  void serialize_ReturnsDate_ForOtherTypes() {
    assertThrows(CoercingSerializeException.class, () -> coercing.serialize(123, DEFAULT_CONTEXT, DEFAULT_LOCALE));
  }

  @Test
  void parseValue_ThrowsException_ForDeprecatedSignature() {
    var value = new Object();
    assertThrows(UnsupportedOperationException.class, () -> coercing.parseValue(value));
  }

  @Test
  void parseValue_ThrowsException() {
    var value = new Object();
    assertThrows(CoercingSerializeException.class, () -> coercing.parseValue(value, DEFAULT_CONTEXT, DEFAULT_LOCALE));
  }

  @Test
  void parseLiteral_ThrowsException_ForDeprecatedSignature() {
    var value = new Object();
    assertThrows(UnsupportedOperationException.class, () -> coercing.parseLiteral(value));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForNowLiteral() {
    var dateTime = coercing.parseLiteral(new StringValue("NOW"), EMPTY_VARIABLES, DEFAULT_CONTEXT, DEFAULT_LOCALE)
        .get();

    assertThat(dateTime.getYear(), equalTo(OffsetDateTime.now()
        .getYear()));
    assertThat(dateTime.getMonth(), equalTo(OffsetDateTime.now()
        .getMonth()));
    assertThat(dateTime.getDayOfMonth(), equalTo(OffsetDateTime.now()
        .getDayOfMonth()));
  }

  @Test
  void parseLiteral_ReturnsDateTime_ForOffsetDateTime() {
    var dateTime = coercing.parseLiteral(new StringValue(OffsetDateTime.now()
        .toString()), EMPTY_VARIABLES, DEFAULT_CONTEXT, DEFAULT_LOCALE)
        .get();

    assertThat(dateTime.getYear(), equalTo(OffsetDateTime.now()
        .getYear()));
    assertThat(dateTime.getMonth(), equalTo(OffsetDateTime.now()
        .getMonth()));
    assertThat(dateTime.getDayOfMonth(), equalTo(OffsetDateTime.now()
        .getDayOfMonth()));
  }
}
