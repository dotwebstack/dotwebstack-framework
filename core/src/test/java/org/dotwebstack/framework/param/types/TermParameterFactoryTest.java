package org.dotwebstack.framework.param.types;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.PropertyShape2;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Ignore
public class TermParameterFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private TermParameterFactory factory;

  @Before
  public void setUp() {
    factory = new TermParameterFactory();
  }

  @Test
  public void createTermParameter_CreatesBooleanTermParameter_ForBooleanShape() {
    // Act
    TermParameter result = factory.createTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        new PropertyShape2(XMLSchema.BOOLEAN, null), false);

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
    TermParameter result = factory.createTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID, "place",
        new PropertyShape2(XMLSchema.STRING, VALUE_FACTORY.createLiteral("foo")), false);

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
    TermParameter result = factory.createTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        new PropertyShape2(XMLSchema.INTEGER, null), true);

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
    TermParameter result = factory.createTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID, "place",
        new PropertyShape2(XMLSchema.ANYURI, defaultValue), true);

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
    factory.createTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        new PropertyShape2(unsupportedDataType, null), false);
  }

}
