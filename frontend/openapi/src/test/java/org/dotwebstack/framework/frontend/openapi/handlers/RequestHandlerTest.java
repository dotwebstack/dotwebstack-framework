package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
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

  private Map<MediaType, Property> schemaMap;

  private RequestHandler requestHandler;

  @Before
  public void setUp() throws URISyntaxException {
    schemaMap = ImmutableMap.of();
    requestHandler = new RequestHandler(apiOperationMock, informationProductMock, schemaMap,
        requestParameterMapperMock, apiRequestValidatorMock, swaggerMock);

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
    assertThat(((TupleEntity) response.getEntity()).getSchemaMap(), equalTo(schemaMap));
    verify(containerRequestMock).setProperty("operation", apiOperationMock.getOperation());
  }

  @Test
  public void apply_ReturnsOkResponse_ForGraphQueryResult() {
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
    Response response = requestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getEntity(), instanceOf(GraphEntity.class));

    assertThat(((GraphEntity) response.getEntity()).getInformationProduct(),
        sameInstance(informationProductMock));
    assertThat(((GraphEntity) response.getEntity()).getParameters(), is(parameters));
    assertThat(((GraphEntity) response.getEntity()).getSchemaMap(), is(schemaMap));
  }

  @Test
  public void apply_ReturnsOkResponse_WhenResultFoundQueryHasBeenDefinedAndQueryReturnsTrue() {
    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).build();

    GraphQueryResult result = new IteratingGraphQueryResult(ImmutableMap.of(), model);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation().vendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.RESULT_FOUND_QUERY,
            String.format("ASK {?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE))).response(
                Status.OK.getStatusCode(), new io.swagger.models.Response()).response(
                    Status.NOT_FOUND.getStatusCode(), new io.swagger.models.Response());

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    Response response = requestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
  }

  @Test
  public void apply_ThrowsNotFoundEx_WhenResultFoundQueryHasBeenDefinedAndQueryReturnsFalse() {
    // Assert
    exception.expect(NotFoundException.class);

    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    Model model = new ModelBuilder().build();

    GraphQueryResult result = new IteratingGraphQueryResult(ImmutableMap.of(), model);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation().vendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.RESULT_FOUND_QUERY,
            String.format("ASK {?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE))).response(
                Status.OK.getStatusCode(), new io.swagger.models.Response()).response(
                    Status.NOT_FOUND.getStatusCode(), new io.swagger.models.Response());

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    requestHandler.apply(containerRequestMock);
  }

  @Test
  public void apply_ThrowsConfigurationEx_WhenResultFoundQueryHasBeenDefinedWithout404Response() {
    // Assert
    exception.expect(ConfigurationException.class);
    exception.expectMessage("Vendor extension 'x-dotwebstack-result-found-query' has been defined, "
        + "while 404 response is missing");

    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    Model model = new ModelBuilder().build();

    GraphQueryResult result = new IteratingGraphQueryResult(ImmutableMap.of(), model);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation().vendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.RESULT_FOUND_QUERY,
            String.format("ASK {?s <%s> <%s>}", RDF.TYPE, DBEERPEDIA.BREWERY_TYPE))).response(
                Status.OK.getStatusCode(), new io.swagger.models.Response());

    when(apiOperationMock.getOperation()).thenReturn(operation);

    // Act
    requestHandler.apply(containerRequestMock);
  }

  @Test
  public void apply_ThrowsConfigurationEx_When404ResponseHasBeenDefinedWithoutResultFoundQuery() {
    // Assert
    exception.expect(ConfigurationException.class);
    exception.expectMessage("Vendor extension 'x-dotwebstack-result-found-query' is missing, "
        + "while 404 response has been defined");

    // Arrange
    ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);

    Model model = new ModelBuilder().build();

    GraphQueryResult result = new IteratingGraphQueryResult(ImmutableMap.of(), model);
    Map<String, String> parameters = ImmutableMap.of();

    when(informationProductMock.getResult(parameters)).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);

    Operation operation = new Operation().response(Status.OK.getStatusCode(),
        new io.swagger.models.Response()).response(Status.NOT_FOUND.getStatusCode(),
            new io.swagger.models.Response());

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
