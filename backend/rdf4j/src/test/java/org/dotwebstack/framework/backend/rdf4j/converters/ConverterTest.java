package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.BooleanLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.memory.model.CalendarMemLiteral;
import org.junit.jupiter.api.Test;

class ConverterTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private List<CoreConverter<?>> converters = ImmutableList.of(new BooleanConverter(), new DateConverter(),
      new DateTimeConverter(), new IritoIriConverter(), new LongConverter());

  @Test
  void convert_booleanLiteral_toBoolean() {
    // Arrange
    BooleanLiteral boolLiteral = BooleanLiteral.TRUE;

    // Act
    CoreConverter<?> converter = getConverter(boolLiteral);

    // Assert
    assertThat(converter, instanceOf(BooleanConverter.class));
    assertThat(((BooleanConverter) converter).convertLiteral(boolLiteral), is(true));
  }

  @Test
  void convert_dateLiteral_toLocalDate() throws DatatypeConfigurationException {
    // Arrange
    XMLGregorianCalendar calender = DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2000-01-01");
    CalendarMemLiteral calenderLiteral = new CalendarMemLiteral(null, calender);

    // Act
    CoreConverter<?> converter = getConverter(calenderLiteral);

    // Assert
    assertThat(converter, instanceOf(DateConverter.class));
    assertThat(((DateConverter) converter).convertLiteral(calenderLiteral), is(LocalDate.parse("2000-01-01")));
  }


  @Test
  void convert_datetimeLiteral_toZonedDateTime() throws DatatypeConfigurationException {
    // Arrange
    XMLGregorianCalendar calender = DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2000-01-01T20:18:00.000+02:00");
    CalendarMemLiteral calenderLiteral = new CalendarMemLiteral(null, calender);

    // Act
    CoreConverter<?> converter = getConverter(calenderLiteral);

    // Assert
    assertThat(converter, instanceOf(DateTimeConverter.class));
    assertThat(((DateTimeConverter) converter).convertLiteral(calenderLiteral),
        is(ZonedDateTime.parse("2000-01-01T20:18:00.000+02:00")));
  }

  @Test
  void convert_iri_toIri() {
    // Arrange
    IRI iri = VF.createIRI("http://www.brewery.com");

    // Act
    CoreConverter<?> converter = getConverter(iri);

    // Assert
    assertThat(converter, instanceOf(IritoIriConverter.class));
    assertThat(((IritoIriConverter) converter).convert(iri), is(iri));
  }

  private CoreConverter<?> getConverter(Value value) {
    return converters.stream()
        .filter(converter -> converter.supports(value))
        .findFirst()
        .orElse(null);
  }
}
