package org.dotwebstack.framework.frontend.ld.mappers;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint.Builder;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.DynamicEndpointRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.EndpointRequestParameterMapper;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.RequestHandler;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamicEndpointRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Mock
  private DynamicEndpoint dynamicEndpoint;

  @Mock
  private DynamicEndpointResourceProvider dynamicEndpointResourceProvider;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private EndpointRequestParameterMapper endpointRequestParameterMapper;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Mock
  private ParameterMapper parameterMapper;

  private HttpConfiguration httpConfiguration;

  private RequestHandler<DynamicEndpoint> representationRequestHandler;

  private DynamicEndpointRequestMapper dynamicEndpointRequestMapper;


  @Before
  public void setUp() {
    when(dynamicEndpoint.getStage()).thenReturn(stage);
    when(dynamicEndpoint.getStage().getFullPath()).thenReturn(
        "/" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.BASE_PATH.getLabel());
    when(dynamicEndpoint.getPathPattern()).thenReturn(DBEERPEDIA.PATH_PATTERN_VALUE);

    Map<org.eclipse.rdf4j.model.Resource, DynamicEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(DBEERPEDIA.DOC_ENDPOINT, dynamicEndpoint);
    when(dynamicEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    dynamicEndpointRequestMapper = new DynamicEndpointRequestMapper(dynamicEndpointResourceProvider,
        supportedWriterMediaTypesScanner, representationRequestHandlerFactory);
    representationRequestHandler = new DynamicEndpointRequestHandler(dynamicEndpoint,
        endpointRequestParameterMapper, representationResourceProvider);

    when(representationRequestHandlerFactory.newRepresentationRequestHandler(
        isA(DynamicEndpoint.class))).thenReturn(representationRequestHandler);
    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // Arrange / Act
    DynamicEndpointRequestMapper dynamicEndpointRequestMapper =
        new DynamicEndpointRequestMapper(dynamicEndpointResourceProvider,
            supportedWriterMediaTypesScanner, representationRequestHandlerFactory);

    // Assert
    assertThat(dynamicEndpointRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // Act / Arrange
    dynamicEndpointRequestMapper.loadDynamicEndpoints(httpConfiguration);

    // Assert
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    final ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.ORG_HOST
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
    assertThat(resource.getResourceMethods(), hasSize(1));
    assertThat(method.getHttpMethod(), equalTo(HttpMethod.GET));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithoutStage() {
    // Arrange
    dynamicEndpoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DynamicEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(dynamicEndpoint.getIdentifier(), dynamicEndpoint);
    when(dynamicEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndpointRequestMapper.loadDynamicEndpoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithNullStage() {
    // Arrange
    dynamicEndpoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DynamicEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(dynamicEndpoint.getIdentifier(), dynamicEndpoint);
    when(dynamicEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndpointRequestMapper.loadDynamicEndpoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_IgnoreSecondRepresentation_WhenAddedTwice() {
    // Arrange
    DynamicEndpoint endPoint = (DynamicEndpoint) new Builder(DBEERPEDIA.DOC_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).parameterMapper(parameterMapper).stage(stage).build();
    DynamicEndpoint samePathEndpoint = (DynamicEndpoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).parameterMapper(parameterMapper).stage(stage).build();
    Map<org.eclipse.rdf4j.model.Resource, DynamicEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    endPointMap.put(samePathEndpoint.getIdentifier(), samePathEndpoint);
    when(dynamicEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndpointRequestMapper.loadDynamicEndpoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void loadRepresentations_UsesPathDomainParameter_WithMatchAllDomain() {
    // Arrange
    Site site = new Site.Builder(DBEERPEDIA.BREWERIES).build();
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();
    DynamicEndpoint endPoint = (DynamicEndpoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).parameterMapper(parameterMapper).stage(stage).build();

    Map<org.eclipse.rdf4j.model.Resource, DynamicEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    when(dynamicEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndpointRequestMapper.loadDynamicEndpoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    assertThat(resource.getPath(), equalTo("/" + Stage.PATH_DOMAIN_PARAMETER
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
  }

}
