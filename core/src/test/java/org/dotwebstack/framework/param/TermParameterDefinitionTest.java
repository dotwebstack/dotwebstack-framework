package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.dotwebstack.framework.param.term.TermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Test;

public class TermParameterDefinitionTest {

  private static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Test
  public void createOptionalParameter_createsOptionalParameter_WithNullDefaultValue() {
    // Arrange
    ShaclShape shape = new ShaclShape(XMLSchema.STRING, null, ImmutableList.of());
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
        "name",
        new ShaclShape(XMLSchema.STRING, VALUE_FACTORY.createLiteral("bar"), ImmutableList.of()));

    // Act
    TermParameter result = (TermParameter) definition.createOptionalParameter();

    // Assert
    assertThat(result.handle(ImmutableMap.of()), is("bar"));
    assertThat(result.getDefaultValue(), is("bar"));
  }

  @Test
  public void createRequiredParameter_createsRequiredParameter_ForProvidedShape() {
    // Arrange
    IRI foo = VALUE_FACTORY.createIRI(XMLSchema.NAMESPACE, "foo");
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", new ShaclShape(XMLSchema.STRING, foo, ImmutableList.of()));

    // Act
    Parameter result = definition.createRequiredParameter();

    // Assert
    assertThat(result, instanceOf(StringTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(true));
  }

  @Test
  public void createOptionalParameter_createsOptionalParameter_ForProvidedShape() {
    // Arrange
    IRI foo = VALUE_FACTORY.createIRI(XMLSchema.NAMESPACE, "foo");
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", new ShaclShape(XMLSchema.STRING, foo, ImmutableList.of()));

    // Act
    Parameter result = definition.createOptionalParameter();

    // Assert
    assertThat(result, instanceOf(StringTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(false));
  }

}
