package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.param.types.IriTermParameter;
import org.dotwebstack.framework.param.types.StringTermParameter;
import org.dotwebstack.framework.param.types.TermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Test;

public class TermParameterDefinitionTest {

  public static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private PropertyShape getShape(IRI type, String value) {
    return PropertyShape.of(type, VALUE_FACTORY.createLiteral(value), null);
  }

  @Test
  public void createRequiredParameter_createsRequiredParameter_ForDefaultShape() {
    // Arrange
    ParameterDefinition definition =
        new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID, "name",
            getShape(XMLSchema.STRING, "foo"));

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
    // TODO NvD Supply a null default value to the shape
    PropertyShape shape = PropertyShape.of(XMLSchema.STRING, null, ImmutableList.of());
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", shape);

    // Act
    TermParameter result = (TermParameter) definition.createOptionalParameter();

    // Assert
    // TODO NvD Check if the result has a null default value
    assertThat(result, instanceOf(StringTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(false));
    assertThat(result.getDefaultValue(), is(nullValue()));
  }

  @Test
  public void createOptionalParameter_createsOptionalParameter_WithProvidedDefaultValue() {
    // Arrange
    // TODO NvD Supply the default value to the shape

    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", getShape(XMLSchema.STRING, "bar"));

    // Act
    TermParameter result = (TermParameter) definition.createOptionalParameter();

    // Assert
    // TODO NvD Check if the result has the provided default value
    assertThat(result.handle(ImmutableMap.of()), is("bar"));
    assertThat(result.getDefaultValue(), is("bar"));
  }

}
