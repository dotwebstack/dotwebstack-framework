package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableList;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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
  private static final String METHOD = "GET";

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

  @Before
  public void setUp() throws URISyntaxException {

    pathParameters.put(ID, ImmutableList.of(BPG, "someOtherId"));
    pathParameters.put(PATH_PARAMETER, ImmutableList.of(PATH_PARAMETER_VALUE));
    queryParameters.put(QUERY_PARAMETER, ImmutableList.of(QUERY_PARAMETER_VALUE));
    headers.put(HttpHeaders.ACCEPT, ImmutableList.of(ContentType.APPLICATION_JSON.toString()));
    headers.put(HttpHeaders.CONTENT_TYPE,
        ImmutableList.of(ContentType.APPLICATION_JSON.toString()));

    String uri = "/endpoint";
    UriInfo uriInfo = mock(UriInfo.class);

    when(context.getUriInfo()).thenReturn(uriInfo);
    when(context.getHeaders()).thenReturn(headers);

    when(uriInfo.getPathParameters()).thenReturn(pathParameters);
    when(uriInfo.getQueryParameters()).thenReturn(queryParameters);

    Operation operation = mock(Operation.class);
    List<Parameter> parameters = new ArrayList<>();
    BodyParameter parameter = mock(BodyParameter.class);
    ModelImpl schema = mock(ModelImpl.class);
    when(schema.getType()).thenReturn("object");
    when(parameter.getSchema()).thenReturn(schema);
    parameters.add(parameter);
    when(operation.getParameters()).thenReturn(parameters);
    when(apiOperation.getOperation()).thenReturn(operation);
  }

  @Test
  public void constructor_IsPrivate() throws NoSuchMethodException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    Constructor<?> constructor = RequestParameterExtractor.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    exception.expect(InvocationTargetException.class);
    constructor.newInstance();
  }

  @Test
  public void extract_ReturnsRequestParameters_ForValidInput() {
    when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

    RequestParameters result = RequestParameterExtractor.extract(apiOperation, swagger, context);

    assertThat(result.get(ID), is(BPG));
    assertThat(result.get(PATH_PARAMETER), is(PATH_PARAMETER_VALUE));
    Assert.assertNull(result.get(RequestParameterExtractor.PARAM_PAGE_NUM));
    Assert.assertNull(result.get(RequestParameterExtractor.PARAM_PAGE_SIZE));
    assertThat(result.get(HttpHeaders.ACCEPT), is(ContentType.APPLICATION_JSON.toString()));
  }

  @Test
  public void extract_ThrowsException_WithNullEntityStream() {
    exception.expect(InternalServerErrorException.class);

    when(context.getEntityStream()).thenReturn(null);

    RequestParameterExtractor.extract(apiOperation, swagger, context);
  }

  @Test
  public void extract_DoesNotFail_WhenNonGeoJsonBodyIsSupplied() {
    String body = "{ \"foo\": \"bar\" }";

    Operation operation = mock(Operation.class);

    when(apiOperation.getOperation()).thenReturn(operation);
    when(context.getEntityStream()).thenReturn(new ByteArrayInputStream(body.getBytes()));

    RequestParameters result = RequestParameterExtractor.extract(apiOperation, swagger, context);

    assertThat(result.get(RequestParameterExtractor.PARAM_GEOMETRY_QUERYTYPE), nullValue());
    assertThat(result.get(RequestParameterExtractor.PARAM_GEOMETRY), nullValue());
    assertThat(result.getRawBody(), is(body));
  }

}

