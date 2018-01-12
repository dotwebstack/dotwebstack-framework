package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import org.dotwebstack.framework.param.shapes.IriPropertyShape;
import org.dotwebstack.framework.param.types.IriTermParameter;
import org.dotwebstack.framework.param.types.StringTermParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;

public class TermParameterDefinitionTest {

  @Test
  public void createRequiredParameter_createsRequiredParameter_ForDefaultShape() {
    // Arrange
    ParameterDefinition definition =
        new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID, "name", Optional.empty());

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
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", Optional.of(new IriPropertyShape()));

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
    ParameterDefinition definition = new TermParameterDefinition(DBEERPEDIA.NAME_PARAMETER_ID,
        "name", Optional.of(new IriPropertyShape()));

    // Act
    Parameter result = definition.createOptionalParameter();

    // Assert
    assertThat(result, instanceOf(IriTermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(result.getName(), is("name"));
    assertThat(result.isRequired(), is(false));
  }

}
