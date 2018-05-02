package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.term.IntegerTermParameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParameterUtilsTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private static final String NAMESPACE_RO = "http://data.informatiehuisruimte.nl/def/ro#";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getParameter_ReturnsParameter_ForId() {
    // Arrange
    Collection<Parameter> parameterCollection = new ArrayList<>();

    StringTermParameter contentCrsParam = new StringTermParameter(
        VALUE_FACTORY.createIRI(NAMESPACE_RO, "ContentCrsParameter"), "Content-Crs", true);
    IntegerTermParameter pageParam =
        new IntegerTermParameter(VALUE_FACTORY.createIRI(NAMESPACE_RO, "xPaginationPageParameter"),
            "x-Pagination-Page", false);
    parameterCollection.add(contentCrsParam);
    parameterCollection.add(pageParam);

    // Act
    Parameter result = ParameterUtils.getParameter(parameterCollection,
        NAMESPACE_RO + "xPaginationPageParameter");

    // Assert
    assertThat(result, sameInstance(pageParam));
  }

  @Test
  public void getParameter_ThrowsConfigurationExceptin_ForUnknownId() {
    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No parameter found for vendor extension value: '%s'",
        NAMESPACE_RO + "ContentCrsParameter"));

    // Arrange
    Collection<Parameter> parameterCollection = new ArrayList<>();
    parameterCollection.add(new StringTermParameter(
        VALUE_FACTORY.createIRI("http://foo#", "bar"), "bar", true));
    parameterCollection.add(new StringTermParameter(
        VALUE_FACTORY.createIRI("http://baz#", "qux"), "qux", false));

    // Act
    ParameterUtils.getParameter(parameterCollection, NAMESPACE_RO + "ContentCrsParameter");
  }

}
