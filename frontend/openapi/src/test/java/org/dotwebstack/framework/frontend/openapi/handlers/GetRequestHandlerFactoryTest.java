package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import io.swagger.models.Operation;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.template.TemplateProcessor;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetRequestHandlerFactoryTest {

  @Mock
  private RequestParameterMapper requestParameterMapperMock;

  @Mock
  private TemplateProcessor templateProcessorMock;

  private GetRequestHandlerFactory getRequestHandlerFactory;

  @Before
  public void setUp() {
    getRequestHandlerFactory = new GetRequestHandlerFactory(requestParameterMapperMock);
  }

  @Test
  public void newGetRequestHandler_createsGetRequestHandler_WithValidData() {
    // Arrange
    Operation operation = new Operation();
    InformationProduct product = new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
            DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH, ImmutableList.of(),
        templateProcessorMock);
    // Act
    GetRequestHandler result = getRequestHandlerFactory.newGetRequestHandler(operation, product);

    // Assert
    assertThat(result.getInformationProduct(), sameInstance(product));
  }

}
