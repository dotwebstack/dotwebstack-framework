package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import freemarker.template.Template;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Before;
import org.junit.Ignore;
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
  private DirectEndpoint endPoint;

  @Mock
  private Representation representation;

  @Mock
  private Stage stage;

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  private EndpointRequestParameterMapper endpointRequestParameterMapper;

  private RequestHandler<DirectEndpoint> getRequestHandler;

  @Mock
  private MultivaluedMap<String, String> headerMap;

  @Mock
  private Template template;

  @Before
  public void setUp() {
    endpointRequestParameterMapper = new EndpointRequestParameterMapper();
    getRequestHandler = new DirectEndpointRequestHandler(endPoint, endpointRequestParameterMapper,
            representationResourceProvider);
    when(endPoint.getGetRepresentation()).thenReturn(representation);
    when(endPoint.getPostRepresentation()).thenReturn(representation);
    when(representation.getInformationProduct()).thenReturn(informationProduct);
  }

  @Test
  public void apply_ReturnQueryRepresentation_WhenGraphQueryResult() {
    // Arrange
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.GRAPH);

    init(HttpMethod.GET,false);

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
  public void apply_ReturnStringForGraph_WhenContainerContextContainsAcceptHtml() {
    // Arrange
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    arrangeMocksHtml(template);

    init(HttpMethod.GET, true);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(String.class));
  }

  @Test
  public void apply_Return406ForGraph_WhenNoTemplateFoundForAcceptHtml() {
    // Arrange
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    arrangeMocksHtml(null);

    init(HttpMethod.GET, true);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(406));
  }

  @Test
  public void apply_ReturnStringForTuple_WhenContainerContextContainsAcceptHtml() {
    // Arrange
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    arrangeMocksHtml(template);

    init(HttpMethod.GET, true);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(String.class));
  }

  @Test
  public void apply_Return406ForTuple_WhenNoTemplateFoundForAcceptHtml() {
    // Arrange
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    arrangeMocksHtml(null);

    init(HttpMethod.GET, true);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(406));
  }


  @Test
  public void apply_ReturnQueryRepresentation_WhenTupleQueryResultGetRequest() {
    // Arrange
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.TUPLE);

    init(HttpMethod.GET, false);

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
    when(representation.getStage()).thenReturn(stage);

    UriInfo uriInfo = mock(UriInfo.class);
    final String appliesTo = "subjectValue";
    MultivaluedMap<String, String> parameterValues = new MultivaluedHashMap<>();
    parameterValues.putSingle("subject", appliesTo);
    Map<Resource, Representation> allRepresentations = new HashMap<>();
    allRepresentations.put(representation.getIdentifier(), representation);

    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    when(containerRequestContext.getRequest().getMethod()).thenReturn(HttpMethod.GET);
    DynamicEndpoint dynamicEndpoint = mock(DynamicEndpoint.class);
    when(dynamicEndpoint.getParameterMapper()).thenReturn(mock(ParameterMapper.class));
    when(dynamicEndpoint.getStage()).thenReturn(stage);
    when(representationResourceProvider.getAll()).thenReturn(allRepresentations);
    when(representation.getAppliesTo()).thenReturn(ImmutableList.of(appliesTo));
    when(representation.getStage().getIdentifier()).thenReturn(mock(IRI.class));
    when(dynamicEndpoint.getStage().getIdentifier()).thenReturn(mock(IRI.class));
    RequestHandler<DynamicEndpoint> requestHandler = new DynamicEndpointRequestHandler(
        dynamicEndpoint, endpointRequestParameterMapper, representationResourceProvider);

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

    init(HttpMethod.POST, false);

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
  public void apply_ConfigurationException_WhenUnsupportedRequestDirectEndpoint() {
    // Arrange
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Result type %s not supported for endpoint %s",
        HttpMethod.PUT, DBEERPEDIA.DOC_ENDPOINT));

    init(HttpMethod.PUT, false);
    when(endPoint.getIdentifier()).thenReturn(DBEERPEDIA.DOC_ENDPOINT);

    // Act
    getRequestHandler.apply(containerRequestContext);
  }

  @Test
  public void apply_ConfigurationException_WhenUnsupportedRequestDynamicEndpoint() {
    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Result type %s not supported for endpoint %s",
        HttpMethod.PUT, DBEERPEDIA.DOC_ENDPOINT));

    // Arrange
    init(HttpMethod.PUT, false);
    DynamicEndpoint dynamicEndpoint = mock(DynamicEndpoint.class);
    when(dynamicEndpoint.getIdentifier()).thenReturn(DBEERPEDIA.DOC_ENDPOINT);
    RequestHandler<DynamicEndpoint> requestHandler = new DynamicEndpointRequestHandler(
        dynamicEndpoint, endpointRequestParameterMapper, representationResourceProvider);

    // Act
    requestHandler.apply(containerRequestContext);
  }

  @Test
  public void apply_ThrowsException_WhenQueryResultIsUnexpected() {
    // Arrange
    Object queryResult = new Object();
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    ResultType resultType = mock(ResultType.class);
    when(informationProduct.getResultType()).thenReturn(resultType);

    init(HttpMethod.GET, false);

    // Assert
    thrown.expect(ClassCastException.class);

    // Act
    getRequestHandler.apply(containerRequestContext);
  }

  private void arrangeMocksHtml(Template template) {
    when(representation.getHtmlTemplate()).thenReturn(template);
  }

  private void init(String get, boolean html) {
    UriInfo uriInfo = mock(UriInfo.class);
    MultivaluedMap<String, String> parameterValues = mock(MultivaluedMap.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(containerRequestContext.getUriInfo().getPathParameters()).thenReturn(parameterValues);
    when(containerRequestContext.getRequest()).thenReturn(mock(Request.class));
    if (html) {
      List<Variant> reqVariants = Variant.mediaTypes(MediaType.TEXT_HTML_TYPE).build();
      when(containerRequestContext.getRequest().selectVariant(reqVariants))
          .thenReturn(Variant.mediaTypes(MediaType.TEXT_HTML_TYPE).build().get(0));
      when(uriInfo.getPath()).thenReturn("/test/");
    } else {
      when(uriInfo.getPath()).thenReturn("/");
    }
    when(containerRequestContext.getRequest().getMethod()).thenReturn(get);
  }

}
