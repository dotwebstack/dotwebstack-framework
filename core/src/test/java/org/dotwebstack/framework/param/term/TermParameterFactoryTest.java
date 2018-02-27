package org.dotwebstack.framework.param.term;

import static org.dotwebstack.framework.param.term.TermParameterFactory.newTermParameter;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.ShaclShape;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.SHACL;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TermParameterFactoryTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void createTermParameter_CreatesBooleanTermParameter_ForBooleanShape() {
    // Act
    TermParameter result = newTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        new ShaclShape(XMLSchema.BOOLEAN, null, ImmutableList.of()), false);

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
    TermParameter result = newTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID, "place",
        new ShaclShape(XMLSchema.STRING, VALUE_FACTORY.createLiteral("foo"), ImmutableList.of()),
        false);

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
    TermParameter result = newTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        new ShaclShape(XMLSchema.INTEGER, null, ImmutableList.of()), true);

    // Assert
    assertThat(result, instanceOf(IntegerTermParameter.class));
    assertThat(result.getDefaultValue(), nullValue());
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(true));
  }

  @Test
  public void createTermParameter_ThrowsException_ForUnknownShape() {
    // Arrange
    IRI unsupportedDataType = VALUE_FACTORY.createIRI("http://unsupported-data-type");

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("Unsupported data type: <%s>. Supported types: %s", unsupportedDataType,
            ImmutableList.of(XMLSchema.BOOLEAN, XMLSchema.STRING, XMLSchema.INTEGER, SHACL.IRI)));

    // Act
    newTermParameter(DBEERPEDIA.NAME_PARAMETER_ID, "name",
        new ShaclShape(unsupportedDataType, null, ImmutableList.of()), false);
  }

}
