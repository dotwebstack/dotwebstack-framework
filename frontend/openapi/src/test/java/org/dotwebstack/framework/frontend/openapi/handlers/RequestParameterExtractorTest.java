package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestParameterExtractorTest {

  private static final String PATH_PARAMETER = "pathParameter";
  private static final String PATH_PARAMETER_VALUE = "pathParameterValue";
  private static final String QUERY_PARAMETER = "queryParameter";
  private static final String QUERY_PARAMETER_VALUE = "queryParameterValue";

  private static final String BPG = "bestemmingsplangebied.1";
  private static final String ID = "id";

  private final ContainerRequestContext context = mock(ContainerRequestContext.class);

  private MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();
  private MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
  private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private Swagger swagger;

  @Mock
  private ApiOperation apiOperation;

  @Mock
  private Operation operation;

  private RequestParameterExtractor requestParameterExtractor;

  @Before
  public void setUp() throws URISyntaxException {

    pathParameters.put(ID, ImmutableList.of(BPG, "someOtherId"));
    pathParameters.put(PATH_PARAMETER, ImmutableList.of(PATH_PARAMETER_VALUE));
    queryParameters.put(QUERY_PARAMETER, ImmutableList.of(QUERY_PARAMETER_VALUE));
    headers.put(HttpHeaders.ACCEPT, ImmutableList.of(ContentType.APPLICATION_JSON.toString()));
    headers.put(HttpHeaders.CONTENT_TYPE,
        ImmutableList.of(ContentType.APPLICATION_JSON.toString()));

    UriInfo uriInfo = mock(UriInfo.class);

    when(context.getUriInfo()).thenReturn(uriInfo);
    when(context.getHeaders()).thenReturn(headers);

    when(uriInfo.getPathParameters()).thenReturn(pathParameters);
    when(uriInfo.getQueryParameters()).thenReturn(queryParameters);

    ModelImpl schema = mock(ModelImpl.class);
    when(schema.getType()).thenReturn("object");

    BodyParameter parameter = mock(BodyParameter.class);

    when(parameter.getSchema()).thenReturn(schema);
    when(parameter.getIn()).thenReturn("body");

    when(operation.getParameters()).thenReturn(ImmutableList.of(parameter));
    when(apiOperation.getOperation()).thenReturn(operation);

    requestParameterExtractor = new RequestParameterExtractor(new ObjectMapper());
  }

  @Test
  public void extract_ReturnsRequestParameters_ForValidInput() {
    // Arrange
    when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

    // Act
    RequestParameters result = requestParameterExtractor.extract(apiOperation, swagger, context);

    // Assert
    assertThat(result.get(ID), is(BPG));
    assertThat(result.get(PATH_PARAMETER), is(PATH_PARAMETER_VALUE));
    Assert.assertNull(result.get(RequestParameterExtractor.PARAM_PAGE_NUM));
    Assert.assertNull(result.get(RequestParameterExtractor.PARAM_PAGE_SIZE));
    assertThat(result.get(HttpHeaders.ACCEPT), is(ContentType.APPLICATION_JSON.toString()));
  }

  @Test
  public void extract_ThrowsException_WithNullEntityStream() {
    // Assert
    exception.expect(InternalServerErrorException.class);

    // Arrange
    when(context.getEntityStream()).thenReturn(null);

    // Act
    requestParameterExtractor.extract(apiOperation, swagger, context);
  }

  @Test
  public void extract_ExtractsSingleBodyParameter_AsMultipleRequestParameters() {
    // Arrange
    String body =
        "{ \"intersects\": { \"type\": \"Point\", \"coordinates\": [5.7,52.8]}, \"foo\": \"bar\"}";

    when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(body.getBytes()));

    // Act
    RequestParameters result = requestParameterExtractor.extract(apiOperation, swagger, context);

    // Assert
    assertThat(result.getRawBody(), is(body));
    assertThat(result.get("intersects"), is("{\"type\":\"Point\",\"coordinates\":[5.7,52.8]}"));
    assertThat(result.get("foo"), is("\"bar\""));
  }

}

