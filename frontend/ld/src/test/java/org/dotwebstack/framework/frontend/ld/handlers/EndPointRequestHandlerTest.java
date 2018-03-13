package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
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
public class EndPointRequestHandlerTest {

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

  private EndPointRequestHandler getRequestHandler;

  @Before
  public void setUp() {
    endPointRequestParameterMapper = new EndPointRequestParameterMapper();
    getRequestHandler = new EndPointRequestHandler(endPoint, endPointRequestParameterMapper,
        representationResourceProvider);
    when(endPoint.getGetRepresentation()).thenReturn(representation);
    when(representation.getInformationProduct()).thenReturn(informationProduct);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRepresentation() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new EndPointRequestHandler(null, endPointRequestParameterMapper,
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
  public void apply_ReturnQueryRepresentation_WhenTupleQueryResult() {
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
