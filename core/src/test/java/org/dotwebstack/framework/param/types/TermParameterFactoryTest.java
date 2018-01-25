package org.dotwebstack.framework.param.types;

import static org.dotwebstack.framework.param.types.TermParameterFactory.getTermParameter;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.PropertyShape;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TermParameterFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Test
  public void createTermParameter_CreatesBooleanTermParameter_ForBooleanShape() {
    // Act
    TermParameter result = getTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        PropertyShape.of(XMLSchema.BOOLEAN, null, null), false);

    // Assert
    assertThat(result, instanceOf(BooleanTermParameter.class));
    assertThat(result.getDefaultValue(), nullValue());
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(false));
  }

  @Test
  public void createTermParameter_CreatesStringTermParameter_ForStringShape() {
    // Act
    TermParameter result = getTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID, "place",
        PropertyShape.of(XMLSchema.STRING, VALUE_FACTORY.createLiteral("foo"), null), false);

    // Assert
    assertThat(result, instanceOf(StringTermParameter.class));
    assertThat(result.getDefaultValue(), is("foo"));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.PLACE_PARAMETER_ID));
    assertThat(result.getName(), is("place"));
    assertThat(result.isRequired(), is(false));
  }

  @Test
  public void createTermParameter_CreatesIntTermParameter_ForIntegerShape() {
    // Act
    TermParameter result = getTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        PropertyShape.of(XMLSchema.INTEGER, null, null), true);

    // Assert
    assertThat(result, instanceOf(IntTermParameter.class));
    assertThat(result.getDefaultValue(), nullValue());
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(true));
  }

  @Test
  public void createTermParameter_CreatesIriTermParameter_ForIriShape() {
    // Arrange
    IRI defaultValue = VALUE_FACTORY.createIRI("http://default-value");

    // Act
    TermParameter result = getTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID, "place",
        PropertyShape.of(XMLSchema.ANYURI, defaultValue, null), true);

    // Assert
    assertThat(result, instanceOf(IriTermParameter.class));
    assertThat(result.getDefaultValue(), is(defaultValue));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.PLACE_PARAMETER_ID));
    assertThat(result.getName(), is("place"));
    assertThat(result.isRequired(), is(true));
  }

  @Test
  public void createTermParameter_ThrowsException_ForUnknownShape() {
    // Arrange
    IRI unsupportedDataType = VALUE_FACTORY.createIRI("http://unsupported-data-type");

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Unsupported data type: <%s>. Supported types: %s",
        unsupportedDataType, ImmutableList.of(XMLSchema.BOOLEAN, XMLSchema.STRING,
            XMLSchema.INTEGER, XMLSchema.ANYURI)));

    // Act
    getTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        PropertyShape.of(unsupportedDataType, null, null), false);
  }

}
