package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InformationProductTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private TemplateProcessor templateProcessorMock;

  @Test
  public void constructor_ThrowsException_IdentifierMissing() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    new TestInformationProduct(null, DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH);
  }

  @Test
  public void constructor_ThrowsException_ResultTypeMissing() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Arrange & Act
    new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), null);
  }

  @Test
  public void constructor_Instantiates_WithValidData() {
    // Arrange & Act
    TestInformationProduct informationProduct =
        new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
            DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH);

    // Assert
    assertThat(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, equalTo(informationProduct.getIdentifier()));
    assertThat(DBEERPEDIA.BREWERIES_LABEL.stringValue(), equalTo(informationProduct.getLabel()));
    assertThat(ResultType.GRAPH, equalTo(informationProduct.getResultType()));
  }

  public class TestInformationProduct extends AbstractInformationProduct {

    protected TestInformationProduct(IRI identifier, String label, ResultType resultType) {
      super(identifier, label, resultType, ImmutableList.of(), templateProcessorMock);
    }

    @Override
    public Object getResult(Map<String, String> parameterValues) {
      return null;
    }

  }

}
