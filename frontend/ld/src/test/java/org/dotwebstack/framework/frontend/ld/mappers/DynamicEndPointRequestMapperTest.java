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
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPoint.Builder;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestParameterMapper;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
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
public class DynamicEndPointRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private DynamicEndPointRequestMapper dynamicEndPointRequestMapper;

  @Mock
  private Stage stage;

  @Mock
  private DynamicEndPoint dynamicEndPoint;

  @Mock
  private DynamicEndPointResourceProvider dynamicEndPointResourceProvider;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private EndPointRequestParameterMapper endPointRequestParameterMapper;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Mock
  private ParameterMapper parameterMapper;

  private HttpConfiguration httpConfiguration;

  private RepresentationRequestHandler representationRequestHandler;

  @Before
  public void setUp() {
    when(dynamicEndPoint.getStage()).thenReturn(stage);
    when(dynamicEndPoint.getStage().getFullPath()).thenReturn(
        "/" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.BASE_PATH.getLabel());
    when(dynamicEndPoint.getPathPattern()).thenReturn(DBEERPEDIA.PATH_PATTERN_VALUE);

    Map<org.eclipse.rdf4j.model.Resource, DynamicEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(DBEERPEDIA.DOC_ENDPOINT, dynamicEndPoint);
    when(dynamicEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    dynamicEndPointRequestMapper = new DynamicEndPointRequestMapper(dynamicEndPointResourceProvider,
        supportedWriterMediaTypesScanner, representationRequestHandlerFactory);
    representationRequestHandler = new RepresentationRequestHandler(dynamicEndPoint,
        endPointRequestParameterMapper, representationResourceProvider);

    when(representationRequestHandlerFactory.newRepresentationRequestHandler(
        isA(AbstractEndPoint.class))).thenReturn(representationRequestHandler);
    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // Arrange / Act
    DynamicEndPointRequestMapper dynamicEndPointRequestMapper =
        new DynamicEndPointRequestMapper(dynamicEndPointResourceProvider,
            supportedWriterMediaTypesScanner, representationRequestHandlerFactory);

    // Assert
    assertThat(dynamicEndPointRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // Act / Arrange
    dynamicEndPointRequestMapper.loadDynamicEndPoints(httpConfiguration);

    // Assert
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    final ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.ORG_HOST
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
    assertThat(resource.getResourceMethods(), hasSize(1));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithoutStage() {
    // Arrange
    dynamicEndPoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DynamicEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(dynamicEndPoint.getIdentifier(), dynamicEndPoint);
    when(dynamicEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndPointRequestMapper.loadDynamicEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithNullStage() {
    // Arrange
    dynamicEndPoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DynamicEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(dynamicEndPoint.getIdentifier(), dynamicEndPoint);
    when(dynamicEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndPointRequestMapper.loadDynamicEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_IgnoreSecondRepresentation_WhenAddedTwice() {
    // Arrange
    DynamicEndPoint endPoint = (DynamicEndPoint) new Builder(DBEERPEDIA.DOC_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).parameterMapper(parameterMapper).stage(stage).build();
    DynamicEndPoint samePathEndPoint = (DynamicEndPoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).parameterMapper(parameterMapper).stage(stage).build();
    Map<org.eclipse.rdf4j.model.Resource, DynamicEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    endPointMap.put(samePathEndPoint.getIdentifier(), samePathEndPoint);
    when(dynamicEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndPointRequestMapper.loadDynamicEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void loadRepresentations_UsesPathDomainParameter_WithMatchAllDomain() {
    // Arrange
    Site site = new Site.Builder(DBEERPEDIA.BREWERIES).build();
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();
    DynamicEndPoint endPoint = (DynamicEndPoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).parameterMapper(parameterMapper).stage(stage).build();

    Map<org.eclipse.rdf4j.model.Resource, DynamicEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    when(dynamicEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    dynamicEndPointRequestMapper.loadDynamicEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    assertThat(resource.getPath(), equalTo("/" + Stage.PATH_DOMAIN_PARAMETER
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
  }

}
