package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
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
public class InformationProductRequestHandlerTest {

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
  private InformationProductRequestParameterMapper requestParameterMapperMock;

  @Mock
  private OpenAPI openApiMock;

  private ApiResponse entityResponse;

  private InformationProductRequestHandler informationProductRequestHandler;

  @Before
  public void setUp() throws URISyntaxException {
    entityResponse = new ApiResponse();
    informationProductRequestHandler = new InformationProductRequestHandler(apiOperationMock,
        informationProductMock, new ApiResponse(), requestParameterMapperMock,
        apiRequestValidatorMock, openApiMock);

    when(containerRequestMock.getBaseUri()).thenReturn(new URI("http://host:123/path"));

    when(apiRequestValidatorMock.validate(apiOperationMock, containerRequestMock)) //
        .thenReturn(new RequestParameters());
    when(apiOperationMock.getOperation()).thenReturn(new Operation());
    when(containerRequestMock.getRequestHeaders()).thenReturn(mock(MultivaluedStringMap.class));

    when(openApiMock.getServers()).thenReturn(Collections.singletonList(new Server().url("")));
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
    Response response = informationProductRequestHandler.apply(containerRequestMock);

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

    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    GraphQueryResult result = mock(GraphQueryResult.class);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation() //
        .extensions(ImmutableMap.of( //
            OpenApiSpecificationExtensions.SUBJECT_QUERY, //
            "SELECT ?s WHERE { ?s ?p ?o }")) //
        .responses(new ApiResponses() //
            .addApiResponse(Status.OK.toString(), new ApiResponse()));

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    Response response = informationProductRequestHandler.apply(containerRequestMock);

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

    Operation operation = new Operation().extensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT_QUERY,
            String.format("SELECT ?s WHERE {?s <%s> <%s> }", RDF.TYPE,
                DBEERPEDIA.BREWERY_TYPE))).responses(
                    new ApiResponses().addApiResponse(Status.OK.toString(),
                        new ApiResponse()));

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    Response response = informationProductRequestHandler.apply(containerRequestMock);

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
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    GraphQueryResult result = mock(GraphQueryResult.class);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation =
        new Operation().responses(
            new ApiResponses().addApiResponse(Status.OK.toString(), new ApiResponse()));

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    informationProductRequestHandler.apply(containerRequestMock);
  }

  @Test
  public void apply_ThrowsNotFoundEx_WhenSubjectQueryResultIsEmptyAnd404ResponseIsDefined() {
    // Assert
    exception.expect(NotFoundException.class);

    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    GraphQueryResult result = mock(GraphQueryResult.class);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation() //
        .extensions(ImmutableMap.of(//
            OpenApiSpecificationExtensions.SUBJECT_QUERY, "SELECT ?s WHERE { ?s ?p ?o }")) //
        .responses(new ApiResponses() //
            .addApiResponse("200", new ApiResponse()) //
            .addApiResponse("404", new ApiResponse())); //

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    informationProductRequestHandler.apply(containerRequestMock);
  }

  @Test
  public void apply_ReturnsServerErrorResponseWithoutEntityObject_ForOtherResult() {
    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    // Act
    Response response = informationProductRequestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(),
        equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat(response.getEntity(), nullValue());
  }

}
