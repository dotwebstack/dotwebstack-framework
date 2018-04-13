package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestHandlerTest {

  private static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private ApiOperation apiOperationMock;

  @Mock
  private InformationProduct informationProductMock;

  @Mock
  private ContainerRequest containerRequestMock;

  @Mock
  private ApiRequestValidator apiRequestValidatorMock;

  @Mock
  private RequestParameterMapper requestParameterMapperMock;

  @Mock
  private Swagger swaggerMock;

  private io.swagger.models.Response entityResponse;

  private RequestHandler requestHandler;

  @Before
  public void setUp() throws URISyntaxException {
    entityResponse = new io.swagger.models.Response();
    requestHandler = new RequestHandler(apiOperationMock, informationProductMock,
        new io.swagger.models.Response(), requestParameterMapperMock, apiRequestValidatorMock,
        swaggerMock);

    when(containerRequestMock.getBaseUri()).thenReturn(new URI("http://host:123/path"));

    RequestParameters requestParameters = new RequestParameters();
    when(apiRequestValidatorMock.validate(apiOperationMock, swaggerMock,
        containerRequestMock)).thenReturn(requestParameters);
    Operation operation = new Operation();
    when(apiOperationMock.getOperation()).thenReturn(operation);
    when(containerRequestMock.getRequestHeaders()).thenReturn(mock(MultivaluedStringMap.class));

    when(requestParameterMapperMock.map(same(operation), eq(informationProductMock),
        same(requestParameters))).thenReturn(ImmutableMap.of());
  }

  @Test
  public void apply_ReturnsOkResponse_ForTupleResult() {
    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);
    TupleQueryResult result = mock(TupleQueryResult.class);
    when(informationProductMock.getResult(ImmutableMap.of())).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.TUPLE);

    // Act
    Response response = requestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getEntity(), instanceOf(TupleEntity.class));
    assertThat(((TupleEntity) response.getEntity()).getResult(), equalTo(result));
    assertThat(((TupleEntity) response.getEntity()).getResponse(), equalTo(entityResponse));
    verify(containerRequestMock).setProperty("operation", apiOperationMock.getOperation());
  }

  @Test
  public void apply_ReturnsOkResponseWithEmptySubjects_ForEmptySubjectQueryResult() {
    // Arrange
    when(swaggerMock.getBasePath()).thenReturn("");
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    GraphQueryResult result = mock(GraphQueryResult.class);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation().vendorExtensions(ImmutableMap.of(
        OpenApiSpecificationExtensions.SUBJECT_QUERY, "SELECT ?s WHERE { ?s ?p ?o }")).response(
            Status.OK.getStatusCode(), new io.swagger.models.Response());

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    Response response = requestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getEntity(), instanceOf(GraphEntity.class));

    assertThat(((GraphEntity) response.getEntity()).getRequestContext().getInformationProduct(),
        sameInstance(informationProductMock));
    assertThat(((GraphEntity) response.getEntity()).getRequestContext().getParameters(),
        is(parameters));
    assertThat(((GraphEntity) response.getEntity()).getResponse(), is(entityResponse));
    assertThat(((GraphEntity) response.getEntity()).getSubjects(), is(empty()));
  }

  @Test
  public void apply_ReturnsOkResponseWithSubjects_ForNonEmptySubjectQueryResult() {
    // Arrange
    when(swaggerMock.getBasePath()).thenReturn("");
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    GraphQueryResult result = new IteratingGraphQueryResult(ImmutableMap.of(), ImmutableList.of(
        VALUE_FACTORY.createStatement(DBEERPEDIA.BROUWTOREN, RDF.TYPE, DBEERPEDIA.BREWERY_TYPE),
        VALUE_FACTORY.createStatement(DBEERPEDIA.BROUWTOREN, RDFS.LABEL,
            DBEERPEDIA.BROUWTOREN_NAME),
        VALUE_FACTORY.createStatement(DBEERPEDIA.MAXIMUS, RDF.TYPE, DBEERPEDIA.WINERY_TYPE),
        VALUE_FACTORY.createStatement(DBEERPEDIA.MAXIMUS, RDFS.LABEL, DBEERPEDIA.MAXIMUS_NAME)));

    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation().vendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
            String.format("SELECT ?s WHERE {?s <%s> <%s> }", RDF.TYPE,
                DBEERPEDIA.BREWERY_TYPE))).response(Status.OK.getStatusCode(),
                    new io.swagger.models.Response());

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    Response response = requestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getEntity(), instanceOf(GraphEntity.class));

    assertThat(((GraphEntity) response.getEntity()).getRequestContext().getInformationProduct(),
        sameInstance(informationProductMock));
    assertThat(((GraphEntity) response.getEntity()).getRequestContext().getParameters(),
        is(parameters));
    assertThat(((GraphEntity) response.getEntity()).getResponse(), is(entityResponse));
    assertThat(((GraphEntity) response.getEntity()).getSubjects(), contains(DBEERPEDIA.BROUWTOREN));
  }

  @Test
  public void apply_ThrowsConfigurationEx_WhenSubjectQueryHasNotBeenDefinedForGraphResult() {
    // Assert
    exception.expect(ConfigurationException.class);

    // Arrange
    when(swaggerMock.getBasePath()).thenReturn("");
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    GraphQueryResult result = mock(GraphQueryResult.class);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation =
        new Operation().response(Status.OK.getStatusCode(), new io.swagger.models.Response());

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    requestHandler.apply(containerRequestMock);
  }

  @Test
  public void apply_ThrowsNotFoundEx_WhenSubjectQueryResultIsEmptyAnd404ResponseIsDefined() {
    // Assert
    exception.expect(NotFoundException.class);

    // Arrange
    when(swaggerMock.getBasePath()).thenReturn("");
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    GraphQueryResult result = mock(GraphQueryResult.class);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation().vendorExtensions(ImmutableMap.of(
        OpenApiSpecificationExtensions.SUBJECT_QUERY, "SELECT ?s WHERE { ?s ?p ?o }")).response(
            Status.OK.getStatusCode(), new io.swagger.models.Response()).response(
                Status.NOT_FOUND.getStatusCode(), new io.swagger.models.Response());

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    requestHandler.apply(containerRequestMock);
  }

  @Test
  public void apply_ReturnsServerErrorResponseWithoutEntityObject_ForOtherResult() {
    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    // Act
    Response response = requestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(),
        equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat(response.getEntity(), nullValue());
  }

}
