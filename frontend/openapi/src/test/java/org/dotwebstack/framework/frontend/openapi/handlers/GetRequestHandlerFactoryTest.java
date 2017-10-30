package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Operation;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.informationproduct.InformationProduct;
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

  private GetRequestHandlerFactory getRequestHandlerFactory;

  @Before
  public void setUp() {
    getRequestHandlerFactory =
        new GetRequestHandlerFactory(requestParameterMapperMock);
  }

  @Test
  public void newGetRequestHandler_createsGetRequestHandler_WithValidData() {
    Operation operation = new Operation();
    InformationProduct product = new TestInformationProduct(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT,
        DBEERPEDIA.BREWERIES_LABEL.stringValue(), ResultType.GRAPH, ImmutableList.of(),
        ImmutableList.of());
    Map<MediaType, Property> schemaMap = ImmutableMap.of();

    GetRequestHandler result =
        getRequestHandlerFactory.newGetRequestHandler(operation, product, schemaMap);

    assertThat(result.getInformationProduct(), sameInstance(product));
    assertThat(result.getSchemaMap(), is(schemaMap));
  }

}
