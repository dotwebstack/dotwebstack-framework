package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.dotwebstack.framework.backend.rdf4j.Rdf4jProperties;
import org.dotwebstack.framework.core.NotImplementedException;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.BooleanLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.memory.model.CalendarMemLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConverterTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Mock
  private Literal literal;

  @Mock
  private Rdf4jProperties rdf4jProperties;

  @Mock
  private Rdf4jProperties.DateFormatProperties dateFormatProperties;

  private List<CoreConverter<Value, ?>> converters;

  @BeforeEach
  public void setup() {
    converters = ImmutableList.of(new BooleanConverter(), new LocalDateConverter(rdf4jProperties),
        new DateTimeConverter(rdf4jProperties), new IriConverter(), new LongConverter(), new IntConverter(),
        new DateConverter());
  }

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
    String formatString = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx";

    when(rdf4jProperties.getDateproperties()).thenReturn(dateFormatProperties);
    when(dateFormatProperties.getDatetimeformat()).thenReturn(formatString);

    XMLGregorianCalendar calender = DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2000-01-01T20:18:00.000+02:00");
    CalendarMemLiteral calenderLiteral = new CalendarMemLiteral(null, calender);

    // Act
    CoreConverter<Value, ?> converter = getConverter(calenderLiteral);
    ZonedDateTime actual = ((DateTimeConverter) converter).convertLiteral(calenderLiteral);

    // Assert
    assertEquals(ZonedDateTime.parse("2000-01-01T20:18:00.000+02:00"), actual);
  }

  @Test
  void convert_datetimeLiteralWithMs_toZonedDateTime() throws DatatypeConfigurationException {
    // Arrange
    String formatString = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX";
    String timeZone = "Europe/Berlin";

    XMLGregorianCalendar calender = DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2019-04-10T16:47:49.789661");
    CalendarMemLiteral calenderLiteral = new CalendarMemLiteral(null, calender);

    // Act
    CoreConverter<Value, ?> converter = getConverter(calenderLiteral);

    // Assert
    assertThat(converter, instanceOf(DateTimeConverter.class));
    assertThat(((DateTimeConverter) converter).convertLiteral(calenderLiteral),
        is(ZonedDateTime.parse("2019-04-10T16:47:49.789661+02:00[Europe/Berlin]")));
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
    DateTimeConverter dateTimeConverter = new DateTimeConverter(rdf4jProperties);

    // Act / Assert
    assertThrows(NotImplementedException.class, () -> dateTimeConverter.convertToValue("dateTime"));
  }

  @Test
  void convertLiteral_toDateValue_withDefaultFormat() {
    // Arrange
    DateConverter dateConverter = new DateConverter();

    // Act & Assert
    assertThrows(UnsupportedOperationException.class, () -> dateConverter.convertLiteral(literal));
  }

  @Test
  void convertLiteral_toDateValue_withAmericanFormat() {
    // Assert
    String formatString = "yyyy-dd-MM";
    when(rdf4jProperties.getDateproperties()).thenReturn(dateFormatProperties);
    when(dateFormatProperties.getDateformat()).thenReturn(formatString);
    when(literal.stringValue()).thenReturn("2015-15-05");
    LocalDateConverter converter = new LocalDateConverter(rdf4jProperties);
    LocalDate expected = LocalDate.parse("2015-05-15");

    // Act
    LocalDate actual = converter.convertLiteral(literal);

    // Assert
    assertEquals(expected, actual);
  }

  @Test
  void convertLiteral_toDateValue_withDutchFormat() {
    // Assert
    String formatString = "dd-MM-yyyy";
    when(rdf4jProperties.getDateproperties()).thenReturn(dateFormatProperties);
    when(dateFormatProperties.getDateformat()).thenReturn(formatString);
    when(literal.stringValue()).thenReturn("15-05-2015");
    LocalDateConverter converter = new LocalDateConverter(rdf4jProperties);
    LocalDate expected = LocalDate.parse("2015-05-15");

    // Act
    LocalDate actual = converter.convertLiteral(literal);

    // Assert
    assertEquals(expected, actual);
  }
}
