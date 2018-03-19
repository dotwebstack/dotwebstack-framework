package org.dotwebstack.framework.frontend.ld.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint.Builder;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestParameterMapper;
import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
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
public class LdEndPointRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Mock
  private Site site;

  @Mock
  private DirectEndPoint directEndPoint;

  @Mock
  private DynamicEndPoint dynamicEndPoint;

  @Mock
  private Representation getRepresentation;

  @Mock
  private Representation postRepresentation;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private DirectEndPointResourceProvider directEndPointResourceProvider;

  @Mock
  private DynamicEndPointResourceProvider dynamicEndPointResourceProvider;

  @Mock
  private LdEndPointRequestMapper ldEndPointRequestMapper;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Mock
  private EndPointRequestParameterMapper endPointRequestParameterMapper;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private StageResourceProvider stageResourceProvider;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private TransactionRequestHandlerFactory transactionRequestHandlerFactory;

  private EndPointRequestHandler endPointRequestHandler;

  @Mock
  private EndPointRequestHandlerFactory endPointRequestHandlerFactory;

  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() {
    when(directEndPoint.getIdentifier()).thenReturn(DBEERPEDIA.DOC_ENDPOINT);
    when(directEndPoint.getStage()).thenReturn(stage);
    when(directEndPoint.getStage().getFullPath()).thenReturn("/fullPath");
    when(directEndPoint.getStage().getBasePath()).thenReturn("/basePath");
    when(directEndPoint.getPathPattern()).thenReturn(DBEERPEDIA.PATH_PATTERN_VALUE);
    // when(directEndPoint.getGetRepresentation()).thenReturn(getRepresentation);
    when(directEndPoint.getPostRepresentation()).thenReturn(postRepresentation);
    when(stageResourceProvider.get(any())).thenReturn(stage);
    when(representationResourceProvider.get(any())).thenReturn(getRepresentation);
    when(directEndPointResourceProvider.get(any())).thenReturn(directEndPoint);
    // when(directEndPointResourceProvider.getAll()).thenReturn(
    // ImmutableBiMap.of(DBEERPEDIA.DOC_ENDPOINT, directEndPoint));


    Map<org.eclipse.rdf4j.model.Resource, DirectEndPoint> endPointMap = new HashMap<>();
    // Map<org.eclipse.rdf4j.model.Resource, DynamicEndPoint> dynamicEndPointMap = new HashMap<>();
    endPointMap.put(DBEERPEDIA.DOC_ENDPOINT, directEndPoint);
    when(directEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    ldEndPointRequestMapper =
        new LdEndPointRequestMapper(directEndPointResourceProvider, dynamicEndPointResourceProvider,
            supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner,
            endPointRequestHandlerFactory, transactionRequestHandlerFactory);
    endPointRequestHandler = new EndPointRequestHandler(directEndPoint,
        endPointRequestParameterMapper, representationResourceProvider);
    // when(dynamicEndPointResourceProvider.getAll()).thenReturn(dynamicEndPointMap);

    // when(endPointRequestHandlerFactory.newEndPointRequestHandler(
    // isA(AbstractEndPoint.class))).thenReturn(endPointRequestHandler);
    when(getRepresentation.getInformationProduct()).thenReturn(informationProduct);
    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // Arrange / Act
    LdEndPointRequestMapper ldEndPointRequestMapper =
        new LdEndPointRequestMapper(directEndPointResourceProvider, dynamicEndPointResourceProvider,
            supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner,
            endPointRequestHandlerFactory, transactionRequestHandlerFactory);

    // Assert
    assertThat(ldEndPointRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    // Act
    ldEndPointRequestMapper.loadEndPoints(httpConfiguration);

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
    directEndPoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DirectEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(directEndPoint.getIdentifier(), directEndPoint);
    when(directEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    ldEndPointRequestMapper.loadEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithNullStage() {
    // Arrange
    directEndPoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DirectEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(directEndPoint.getIdentifier(), directEndPoint);
    when(directEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    ldEndPointRequestMapper.loadEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_IgnoreSecondRepresentation_WhenAddedTwice() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    DirectEndPoint endPoint = (DirectEndPoint) new Builder(DBEERPEDIA.DOC_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).getRepresentation(getRepresentation).stage(stage).build();
    DirectEndPoint samePathEndPoint = (DirectEndPoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).getRepresentation(getRepresentation).stage(stage).build();
    Map<org.eclipse.rdf4j.model.Resource, DirectEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    endPointMap.put(samePathEndPoint.getIdentifier(), samePathEndPoint);
    when(directEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    ldEndPointRequestMapper.loadEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void loadRepresentations_UsesPathDomainParameter_WithMatchAllDomain() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    Site site = new Site.Builder(DBEERPEDIA.BREWERIES).build();
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();
    DirectEndPoint endPoint = (DirectEndPoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).getRepresentation(getRepresentation).stage(stage).build();

    Map<org.eclipse.rdf4j.model.Resource, DirectEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    when(directEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    ldEndPointRequestMapper.loadEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    assertThat(resource.getPath(), equalTo("/" + Stage.PATH_DOMAIN_PARAMETER
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));

  }

}
