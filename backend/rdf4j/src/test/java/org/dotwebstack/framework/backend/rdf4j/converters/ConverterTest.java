package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.stream.Stream;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.BooleanLiteral;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sail.memory.model.CalendarMemLiteral;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

class ConverterTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @TestFactory
  Stream<DynamicContainer> converterTest() throws DatatypeConfigurationException {
    CalendarMemLiteral dateLiteral = new CalendarMemLiteral(null, DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2000-01-01"));

    CalendarMemLiteral dateTimeLiteral = new CalendarMemLiteral(null, DatatypeFactory.newInstance()
        .newXMLGregorianCalendar("2000-01-01T20:18:00.000+02:00"));

    return Stream.of(getTestCases(new BooleanConverter(), BooleanLiteral.TRUE, true),
        getTestCases(new ByteConverter(), VF.createLiteral(Byte.parseByte("2")), Byte.parseByte("2")),
        getTestCases(new DateConverter(), dateLiteral, LocalDate.parse("2000-01-01")),
        getTestCases(new DateTimeConverter(), dateTimeLiteral, ZonedDateTime.parse("2000-01-01T20:18:00.000+02:00")),
        getTestCases(new DecimalConverter(), VF.createLiteral(new BigDecimal("3.5")), new BigDecimal("3.5")),
        getTestCases(new DoubleConverter(), VF.createLiteral(3.5D), 3.5D),
        getTestCases(new FloatConverter(), VF.createLiteral(15F), 15F),
        getTestCases(new IntConverter(), VF.createLiteral(1024), 1024),
        getTestCases(new IntegerConverter(), VF.createLiteral(new BigInteger("1024")), new BigInteger("1024")),
        getTestCases(new IriConverter(), VF.createIRI("http://brewery.com"), VF.createIRI("http://brewery.com")),
        getTestCases(new LongConverter(), VF.createLiteral(1024L), 1024L),
        getTestCases(new ShortConverter(), VF.createLiteral((short) 2), (short) 2));
  }

  private DynamicContainer getTestCases(CoreConverter<Value, ?> converter, Value value, Object expected) {
    String className = converter.getClass()
        .getSimpleName();
    return dynamicContainer(className, Stream.of(
        dynamicTest("supports " + value, () -> assertThat(converter.supports(value), is(true))),
        dynamicTest("converts to " + expected.getClass(), () -> assertThat(converter.convert(value), is(expected)))));
  }
}
