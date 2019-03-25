package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.jupiter.api.Test;

class ValueUtilsTest {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static Resource SUBJECT = VF.createIRI("foo:1");

  private static Literal LABEL = VF.createLiteral("foo");

  @Test
  void convertLiteral_ReturnsSpecificType_ForBuiltInScalars() {
    // Arrange
    ImmutableMap.Builder<Class, Literal> builder = ImmutableMap.builder();
    builder.put(String.class, VF.createLiteral(""));
    builder.put(Boolean.class, VF.createLiteral(true));
    builder.put(Integer.class, VF.createLiteral(Integer.MIN_VALUE));
    builder.put(BigInteger.class, VF.createLiteral(BigInteger.ONE));
    builder.put(Short.class, VF.createLiteral(Short.MIN_VALUE));
    builder.put(Long.class, VF.createLiteral(Long.MIN_VALUE));
    builder.put(Float.class, VF.createLiteral(Float.MIN_VALUE));
    builder.put(Double.class, VF.createLiteral(Double.MIN_VALUE));
    builder.put(BigDecimal.class, VF.createLiteral(BigDecimal.ONE));
    builder.put(Byte.class, VF.createLiteral(Byte.MIN_VALUE));
    Map<Class, Literal> literals = builder.build();

    // Act & Assert
    literals.forEach((literalType, literal) -> {
      Object result = ValueUtils.convertLiteral(literal);
      assertThat(result, is(instanceOf(literalType)));
    });
  }

  @Test
  void convertLiteral_ReturnsLiteral_ForNonBuiltInScalars() {
    // Act
    Object result = ValueUtils.convertLiteral(VF.createLiteral(new Date()));

    // Assert
    assertThat(result, is(instanceOf(Literal.class)));
  }

  @Test
  void findRequiredPropertyIri_ReturnsIri_ForPresentValue() {
    // Arrange
    Model model = new ModelBuilder()
        .add(SUBJECT, RDF.TYPE, RDFS.CLASS)
        .build();

    // Act
    IRI property = ValueUtils.findRequiredPropertyIri(model, SUBJECT, RDF.TYPE);

    // Assert
    assertThat(property, is(equalTo(RDFS.CLASS)));
  }

  @Test
  void findRequiredPropertyIri_ThrowsException_ForAbsentValue() {
    // Arrange
    Model model = new ModelBuilder().build();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        ValueUtils.findRequiredPropertyIri(model, SUBJECT, RDF.TYPE));
  }

  @Test
  void findRequiredPropertyLiteral_ReturnsLiteral_ForPresentValue() {
    // Arrange
    Model model = new ModelBuilder()
        .add(SUBJECT, RDFS.LABEL, LABEL)
        .build();

    // Act
    Literal property = ValueUtils.findRequiredPropertyLiteral(model, SUBJECT, RDFS.LABEL);

    // Assert
    assertThat(property, is(equalTo(LABEL)));
  }

  @Test
  void findRequiredPropertyLiteral_ThrowsException_ForAbsentValue() {
    // Arrange
    Model model = new ModelBuilder().build();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () ->
        ValueUtils.findRequiredPropertyLiteral(model, SUBJECT, RDFS.LABEL));
  }

}
