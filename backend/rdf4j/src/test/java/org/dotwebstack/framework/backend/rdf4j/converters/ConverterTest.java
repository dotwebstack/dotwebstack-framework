package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.dotwebstack.framework.core.NotImplementedException;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.BooleanLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.memory.model.CalendarMemLiteral;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConverterTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Mock
  private Literal literal;

  private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private List<CoreConverter<Value, ?>> converters = ImmutableList.of(new BooleanConverter(), new LocalDateConverter(),
      new DateTimeConverter(), new IriConverter(), new LongConverter(), new IntConverter(), new DateConverter());

  @Test
  void convert_booleanLiteral_toBoolean() {
    // Arrange
    BooleanLiteral boolLiteral = BooleanLiteral.TRUE;

    // Act
    CoreConverter<Value, ?> converter = getConverter(boolLiteral);

    // Assert
    assertThat(converter, instanceOf(BooleanConverter.class));
    assertThat(((BooleanConverter) converter).convertLiteral(boolLiteral), is(true));
  }

  @Test
  void convert_integer_Literal_toInteger() {
    // Arrange
    Literal integerLiteral = VF.createLiteral((Integer) 5);

    // Act
    CoreConverter<Value, ?> converter = getConverter(integerLiteral);

    // Assert
    assertThat(converter.convertFromValue(integerLiteral), is(5));

  }

  @Test
  void convert_dateLiteral_toLocalDate() throws DatatypeConfigurationException {
    // Arrange
    XMLGregorianCalendar calender = DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2000-01-01");
    CalendarMemLiteral calenderLiteral = new CalendarMemLiteral(null, calender);

    // Act
    CoreConverter<Value, ?> converter = getConverter(calenderLiteral);

    // Assert
    assertThat(converter, instanceOf(LocalDateConverter.class));
    assertThat(((LocalDateConverter) converter).convertLiteral(calenderLiteral), is(LocalDate.parse("2000-01-01")));
  }

  @Test
  void convert_datetimeLiteral_toZonedDateTime() throws DatatypeConfigurationException {
    // Arrange
    XMLGregorianCalendar calender = DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2000-01-01T20:18:00.000+02:00");
    CalendarMemLiteral calenderLiteral = new CalendarMemLiteral(null, calender);

    // Act
    CoreConverter<Value, ?> converter = getConverter(calenderLiteral);

    // Assert
    assertThat(converter, instanceOf(DateTimeConverter.class));
    assertThat(((DateTimeConverter) converter).convertLiteral(calenderLiteral),
        is(ZonedDateTime.parse("2000-01-01T20:18:00.000+02:00")));
  }

  @Test
  void convert_datetimeLiteralWithMs_toZonedDateTime() throws DatatypeConfigurationException {
    // Arrange
    XMLGregorianCalendar calender = DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2019-04-10T16:47:49.789661");
    CalendarMemLiteral calenderLiteral = new CalendarMemLiteral(null, calender);

    // Act
    CoreConverter<Value, ?> converter = getConverter(calenderLiteral);

    // Assert
    assertThat(converter, instanceOf(DateTimeConverter.class));
    assertThat(((DateTimeConverter) converter).convertLiteral(calenderLiteral),
        is(ZonedDateTime.parse("2019-04-10T16:47:49.789661+02:00[Europe/Amsterdam]")));
  }

  @Test
  void convert_iri_toIri() {
    // Arrange
    IRI iri = VF.createIRI("http://www.brewery.com");

    // Act
    CoreConverter<Value, ?> converter = getConverter(iri);

    // Assert
    assertThat(converter, instanceOf(IriConverter.class));
    assertThat(((IriConverter) converter).convertFromValue(iri), is(iri));
  }

  private CoreConverter<Value, ?> getConverter(Value value) {
    return converters.stream()
        .filter(converter -> converter.supportsValue(value))
        .findFirst()
        .orElse(null);
  }

  @Test
  void convert_dateTime_toValue_shouldThrowNotImplementedException() {
    // Arrange
    DateTimeConverter dateTimeConverter = new DateTimeConverter();

    // Act / Assert
    assertThrows(NotImplementedException.class, () -> dateTimeConverter.convertToValue("dateTime"));
  }

  @Test
  void convertLiteral_toDateValue_withSupportedFormat() {
    // Arrange
    DateConverter dateConverter = new DateConverter();
    when(literal.stringValue()).thenReturn("2020-01-31");

    // Act
    Date date = dateConverter.convertLiteral(literal);

    // Assert
    assertEquals("2020-01-31", simpleDateFormat.format(date));
  }

  @Test
  void convertLiteraltoDateValue_throwsError_withUnSupportedFormat() {
    // Arrange
    DateConverter dateConverter = new DateConverter();
    when(literal.stringValue()).thenReturn("2020-31-01");

    // Act & Assert
    Exception exception = assertThrows(IllegalArgumentException.class, () -> dateConverter.convertLiteral(literal));
    assertEquals("Format for date 2020-31-01 does not have the expected format yyyy-MM-dd", exception.getMessage());
  }

}
