package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPoint;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RepresentationRequestHandlerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private DirectEndPoint endPoint;

  @Mock
  private Representation representation;

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  private EndPointRequestParameterMapper endPointRequestParameterMapper;

  private RepresentationRequestHandler getRequestHandler;

  @Before
  public void setUp() {
    endPointRequestParameterMapper = new EndPointRequestParameterMapper();
    getRequestHandler = new RepresentationRequestHandler(endPoint, endPointRequestParameterMapper,
        representationResourceProvider);
    when(endPoint.getGetRepresentation()).thenReturn(representation);
    when(endPoint.getPostRepresentation()).thenReturn(representation);
    when(representation.getInformationProduct()).thenReturn(informationProduct);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRepresentation() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationRequestHandler(null, endPointRequestParameterMapper,
        representationResourceProvider);
  }

  @Test
  public void apply_ReturnQueryRepresentation_WhenGraphQueryResult() {
    // Arrange
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.GRAPH);

    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap<String, String> parameterValues = mock(MultivaluedMap.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.GET);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(GraphEntity.class));
    GraphEntity entity = (GraphEntity) response.getEntity();
    assertThat(entity.getQueryResult(), equalTo(queryResult));
    assertThat(entity.getRepresentation(), equalTo(endPoint.getGetRepresentation()));
  }

  @Test
  public void apply_ReturnQueryRepresentation_WhenTupleQueryResultGetRequest() {
    // Arrange
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.TUPLE);

    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap<String, String> parameterValues = mock(MultivaluedMap.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.GET);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(TupleEntity.class));
    TupleEntity entity = (TupleEntity) response.getEntity();
    assertThat(entity.getQueryResult(), equalTo(queryResult));
    assertThat(entity.getRepresentation(), equalTo(endPoint.getGetRepresentation()));
  }

  @Test
  public void apply_ReturnQueryRepresentation_WhenTupleQueryResultGetRequestDynamicEndpoint() {
    // Arrange
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(informationProduct.getResult(any())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.TUPLE);
    when(representation.getInformationProduct()).thenReturn(informationProduct);

    UriInfo uriInfo = mock(UriInfo.class);
    final String appliesTo = "subjectValue";
    MultivaluedMap<String, String> parameterValues = new MultivaluedHashMap<>();
    parameterValues.putSingle("subject", appliesTo);
    Map<Resource, Representation> allRepresentations = new HashMap<>();
    allRepresentations.put(representation.getIdentifier(), representation);

    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.GET);
    DynamicEndPoint dynamicEndPoint = mock(DynamicEndPoint.class);
    when(dynamicEndPoint.getParameterMapper()).thenReturn(mock(ParameterMapper.class));
    when(representationResourceProvider.getAll()).thenReturn(allRepresentations);
    when(representation.getAppliesTo()).thenReturn(ImmutableList.of(appliesTo));
    RepresentationRequestHandler requestHandler = new RepresentationRequestHandler(dynamicEndPoint,
        endPointRequestParameterMapper, representationResourceProvider);

    // Act
    Response response = requestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(TupleEntity.class));
    TupleEntity entity = (TupleEntity) response.getEntity();
    assertThat(entity.getQueryResult(), equalTo(queryResult));
  }

  @Test
  public void apply_ReturnQueryRepresentation_WhenTupleQueryResultPostRequest() {
    // Arrange
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.TUPLE);

    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap<String, String> parameterValues = mock(MultivaluedMap.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.POST);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(TupleEntity.class));
    TupleEntity entity = (TupleEntity) response.getEntity();
    assertThat(entity.getQueryResult(), equalTo(queryResult));
    assertThat(entity.getRepresentation(), equalTo(endPoint.getGetRepresentation()));
  }

  @Test
  public void apply_ConfigurationException_WhenUnsupportedRequestDirectEndPoint() {
    // Arrange
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Result type %s not supported for endpoint %s",
        HttpMethod.PUT, DBEERPEDIA.DOC_ENDPOINT));

    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap<String, String> parameterValues = mock(MultivaluedMap.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.PUT);
    when(endPoint.getIdentifier()).thenReturn(DBEERPEDIA.DOC_ENDPOINT);

    // Act
    getRequestHandler.apply(containerRequestContext);
  }

  @Test
  public void apply_ConfigurationException_WhenUnsupportedRequestDynamicEndPoint() {
    // Arrange
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Result type %s not supported for endpoint %s",
        HttpMethod.PUT, DBEERPEDIA.DOC_ENDPOINT));

    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap<String, String> parameterValues = mock(MultivaluedMap.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.PUT);
    DynamicEndPoint dynamicEndPoint = mock(DynamicEndPoint.class);
    when(dynamicEndPoint.getIdentifier()).thenReturn(DBEERPEDIA.DOC_ENDPOINT);
    RepresentationRequestHandler requestHandler = new RepresentationRequestHandler(dynamicEndPoint,
        endPointRequestParameterMapper, representationResourceProvider);

    // Act
    requestHandler.apply(containerRequestContext);
  }

  @Test
  public void apply_ThrowsException_WhenQueryResultIsUnexpected() {
    // Arrange
    Object queryResult = new Object();
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);

    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap<String, String> parameterValues = mock(MultivaluedMap.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.GET);

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    getRequestHandler.apply(containerRequestContext);
  }

}
