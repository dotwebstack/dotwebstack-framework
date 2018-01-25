package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.param.term.IriTermParameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.dotwebstack.framework.param.term.TermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Test;

public class TermParameterDefinitionTest {

  public static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  // XXX (PvH) Suggestie: hernoemen naar new(Property)Shape

  // XXX (PvH) (2) Overigens wel grappig: het feit dat je een convenience method maakt, zou kunnen
  // betekenen dat de factory method ontbreekt op de PropertyShape :-)

  // XXX (PvH) (3) Dit is meer een kwestie van stijl, maar je kan je de toegevoegde waarde afvragen
  // van deze method. Hij wordt tenslotte maar 2x gebruikt.
  // Belangrijker vind ik: de factory method op de PropertyShape gebruiken in je testen vind ik
  // duidelijker dan de convenience method aanroepen, simpelweg omdat ik dan direct zie welke test
  // data je gebruikt.
  private PropertyShape getShape(IRI type, String value) {
    return PropertyShape.of(type, VALUE_FACTORY.createLiteral(value), null);
  }

  // XXX (PvH) Nu we geen default shape meer hebben, kan deze test weg?
  @Test
  public void createRequiredParameter_createsRequiredParameter_ForDefaultShape() {
    // Arrange
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", getShape(XMLSchema.STRING, "foo"));

    // Act
    Parameter result = definition.createRequiredParameter();

    // Assert
    assertThat(result, instanceOf(StringTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(true));
  }

  @Test
  public void createRequiredParameter_createsRequiredParameter_ForProvidedShape() {
    // Arrange
    IRI foo = VALUE_FACTORY.createIRI(XMLSchema.NAMESPACE, "foo");
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", PropertyShape.of(XMLSchema.ANYURI, foo, null));

    // Act
    Parameter result = definition.createRequiredParameter();

    // Assert
    assertThat(result, instanceOf(IriTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(true));
  }

  @Test
  public void createOptionalParameter_createsOptionalParameter_ForProvidedShape() {
    // Arrange
    IRI foo = VALUE_FACTORY.createIRI(XMLSchema.NAMESPACE, "foo");
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", PropertyShape.of(XMLSchema.ANYURI, foo, null));

    // Act
    Parameter result = definition.createOptionalParameter();

    // Assert
    assertThat(result, instanceOf(IriTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(false));
  }

  @Test
  public void createOptionalParameter_createsOptionalParameter_WithNullDefaultValue() {
    // Arrange
    PropertyShape shape = PropertyShape.of(XMLSchema.STRING, null, ImmutableList.of());
    ParameterDefinition definition =
        new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID, "name", shape);

    // Act
    TermParameter result = (TermParameter) definition.createOptionalParameter();

    // Assert
    assertThat(result, instanceOf(StringTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(false));
    assertThat(result.getDefaultValue(), is(nullValue()));
  }

  @Test
  public void createOptionalParameter_createsOptionalParameter_WithProvidedDefaultValue() {
    // Arrange
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", getShape(XMLSchema.STRING, "bar"));

    // Act
    TermParameter result = (TermParameter) definition.createOptionalParameter();

    // Assert
    assertThat(result.handle(ImmutableMap.of()), is("bar"));
    assertThat(result.getDefaultValue(), is("bar"));
  }

}
