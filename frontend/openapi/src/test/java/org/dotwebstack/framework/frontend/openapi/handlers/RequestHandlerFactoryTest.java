package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.ApiPathImpl;
import com.google.common.collect.ImmutableList;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
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
public class RequestHandlerFactoryTest {

  @Mock
  private RequestParameterMapper requestParameterMapperMock;

  @Mock
  private RequestParameterExtractor requestParameterExtractorMock;

  @Mock
  private Swagger swaggerMock;

  @Mock
  private TemplateProcessor templateProcessorMock;

  private RequestHandlerFactory requestHandlerFactory;

  @Before
  public void setUp() {
    requestHandlerFactory =
        new RequestHandlerFactory(requestParameterMapperMock, requestParameterExtractorMock);
  }

  @Test
  public void newGetRequestHandler_createsGetRequestHandler_WithValidData() {
    // Arrange
    Operation operation = new Operation();
    ApiOperation apiOperation = new ApiOperation(new ApiPathImpl("/", ""), new ApiPathImpl("/", ""),
        HttpMethod.GET, operation);
    InformationProduct product = new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH, ImmutableList.of(),
        templateProcessorMock);
    Response response = new Response();

    // Act
    RequestHandler result =
        requestHandlerFactory.newRequestHandler(apiOperation, product, response, swaggerMock);


    // Assert
    assertThat(result.getInformationProduct(), sameInstance(product));
    assertThat(result.getResponse(), is(response));
  }

}
