package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

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
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandlerFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestHandlerFactoryTest {

  @Mock
  private InformationProductRequestParameterMapper requestParameterMapperMock;

  @Mock
  private TransactionRequestParameterMapper transactionRequestParameterMapper;

  @Mock
  private TransactionRequestBodyMapper transactionRequestBodyMapper;

  @Mock
  private RequestParameterExtractor requestParameterExtractorMock;

  @Mock
  private TransactionHandlerFactory transactionHandlerFactory;

  @Mock
  private Swagger swaggerMock;

  @Mock
  private TemplateProcessor templateProcessorMock;

  private RequestHandlerFactory requestHandlerFactory;

  @Before
  public void setUp() {
    requestHandlerFactory =
        new RequestHandlerFactory(requestParameterMapperMock, transactionRequestParameterMapper,
            transactionRequestBodyMapper, requestParameterExtractorMock, transactionHandlerFactory);
  }

  @Test
  public void new_createsInformationProductRequestHandler_WithValidData() {
    // Arrange
    Operation operation = new Operation();
    ApiOperation apiOperation = new ApiOperation(new ApiPathImpl("/", ""), new ApiPathImpl("/", ""),
        HttpMethod.GET, operation);
    InformationProduct product = new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH, ImmutableList.of(),
        templateProcessorMock);
    Response response = new Response();

    // Act
    InformationProductRequestHandler result =
        requestHandlerFactory.newInformationProductRequestHandler(apiOperation, product, response,
            swaggerMock);

    // Assert
    assertThat(result.getInformationProduct(), sameInstance(product));
    assertThat(result.getResponse(), is(response));
  }

  @Test
  public void new_createsTransactionRequestHandler_WithValidData() {
    // Arrange
    Operation operation = new Operation();
    ApiOperation apiOperation = new ApiOperation(new ApiPathImpl("/", ""), new ApiPathImpl("/", ""),
        HttpMethod.POST, operation);
    Resource transactionIdentifier = mock(Resource.class);
    Model model = mock(Model.class);
    Transaction transaction = new Transaction.Builder(transactionIdentifier).build();

    // Act
    TransactionRequestHandler result = requestHandlerFactory
        .newTransactionRequestHandler(apiOperation, transaction, swaggerMock);

    // Assert
    assertThat(result.getTransaction(), sameInstance(transaction));
  }

}
