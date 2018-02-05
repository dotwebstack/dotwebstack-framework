package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.term.IntegerTermParameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InformationProductUtilsTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private static final String NAMESPACE_RO = "http://data.informatiehuisruimte.nl/def/ro#";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getParameter_ReturnsParameter_ForId() {
    // Arrange
    InformationProduct productMock = mock(InformationProduct.class);

    StringTermParameter contentCrsParam = new StringTermParameter(
        VALUE_FACTORY.createIRI(NAMESPACE_RO, "ContentCrsParameter"), "Content-Crs", true);
    IntegerTermParameter pageParam =
        new IntegerTermParameter(VALUE_FACTORY.createIRI(NAMESPACE_RO, "xPaginationPageParameter"),
            "x-Pagination-Page", false);

    when(productMock.getParameters()).thenReturn(ImmutableList.of(contentCrsParam, pageParam));

    // Act
    Parameter result = InformationProductUtils.getParameter(productMock,
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
    InformationProduct productMock = mock(InformationProduct.class);
    when(productMock.getParameters()).thenReturn(ImmutableList.of(
        new StringTermParameter(VALUE_FACTORY.createIRI("http://foo#", "bar"), "bar", true),
        new StringTermParameter(VALUE_FACTORY.createIRI("http://baz#", "qux"), "qux", false)));

    // Act
    InformationProductUtils.getParameter(productMock, NAMESPACE_RO + "ContentCrsParameter");
  }

}
