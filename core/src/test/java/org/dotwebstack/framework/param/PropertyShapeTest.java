package org.dotwebstack.framework.param;

import static com.google.common.collect.ImmutableList.of;
import static org.eclipse.rdf4j.model.vocabulary.XMLSchema.BOOLEAN;
import static org.eclipse.rdf4j.model.vocabulary.XMLSchema.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

import org.dotwebstack.framework.vocabulary.SHACL;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PropertyShapeTest {

  private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
  private static final Literal DEFAULT_VALUE = FACTORY.createLiteral("default");
  private static final Literal LITERAL1 = FACTORY.createLiteral("in1");
  private static final Literal LITERAL2 = FACTORY.createLiteral("in2");

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void create_AllValuesPresent() {
    // Act
    PropertyShape shape = PropertyShape.of(STRING, DEFAULT_VALUE, of(LITERAL1, LITERAL2));

    // Assert
    assertThat(shape.getDatatype(), is(STRING));
    assertThat(shape.getDefaultValue(), is(DEFAULT_VALUE));
    assertThat(shape.getDefaultValue().stringValue(), is("default"));
    assertThat(shape.getIn().size(), is(2));
    assertThat(shape.getIn().containsAll(of(LITERAL1, LITERAL2)), is(true));
  }

  @Test
  public void create_TypeAndDefaultValuePresent() {
    // Act
    PropertyShape shape = PropertyShape.of(STRING, DEFAULT_VALUE, null);

    // Assert
    assertThat(shape.getDatatype(), is(STRING));
    assertThat(shape.getDefaultValue(), is(DEFAULT_VALUE));
    assertThat(shape.getDefaultValue().stringValue(), is("default"));
    assertThat(shape.getIn(), is(nullValue()));
  }

  @Test
  public void create_TypeAndInValuePresent() {
    // Act
    PropertyShape shape = PropertyShape.of(STRING, null, of(LITERAL1, LITERAL2));

    // Assert
    assertThat(shape.getDatatype(), is(STRING));
    assertThat(shape.getDefaultValue(), is(nullValue()));
    assertThat(shape.getIn().size(), is(2));
    assertThat(shape.getIn().containsAll(of(LITERAL1, LITERAL2)), is(true));
  }

  @Test
  public void create_OnlyTypePresent() {
    // Act
    PropertyShape shape = PropertyShape.of(BOOLEAN, null, null);

    // Assert
    assertThat(shape.getDatatype(), is(BOOLEAN));
    assertThat(shape.getDefaultValue(), is(nullValue()));
    assertThat(shape.getIn(), is(nullValue()));
  }

  @Test
  public void create_WrongType() {
    // Arrange
    IRI wrongString = FACTORY.createIRI(SHACL.DEFAULT_VALUE.getNamespace() + "string");

    // Act
    PropertyShape shape = PropertyShape.of(wrongString, null, null);

    // Assert
    assertThat(shape.getDatatype(), is(wrongString));
  }

  @Test
  public void create_TypeMissing_ThrowNpe() {
    // PropertyShape should at least have a type
    // Assert
    exception.expect(NullPointerException.class);
    // Act
    PropertyShape.of(null, null, null);
  }
}