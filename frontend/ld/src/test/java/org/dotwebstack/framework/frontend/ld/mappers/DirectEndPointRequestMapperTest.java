package org.dotwebstack.framework.frontend.ld.mappers;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint.Builder;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.EndPointRequestParameterMapper;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.ServiceRequestHandlerFactory;
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
public class DirectEndPointRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Mock
  private DirectEndPoint directEndPoint;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private DirectEndPointResourceProvider directEndPointResourceProvider;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Mock
  private EndPointRequestParameterMapper endPointRequestParameterMapper;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private ServiceRequestHandlerFactory transactionRequestHandlerFactory;

  @Mock
  private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  private DirectEndPointRequestMapper directEndPointRequestMapper;

  private HttpConfiguration httpConfiguration;

  private Representation getRepresentation;

  private Representation postRepresentation;

  private RepresentationRequestHandler representationRequestHandler;

  @Before
  public void setUp() {
    getRepresentation = new Representation.RepresentationBuilder(
        DBEERPEDIA.BREWERY_REPRESENTATION).informationProduct(informationProduct).build();
    postRepresentation = new Representation.RepresentationBuilder(
        DBEERPEDIA.BREWERY_REPRESENTATION).informationProduct(informationProduct).build();

    when(directEndPoint.getStage()).thenReturn(stage);
    when(directEndPoint.getStage().getFullPath()).thenReturn(
        "/" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.BASE_PATH.getLabel());
    when(directEndPoint.getPathPattern()).thenReturn(DBEERPEDIA.PATH_PATTERN_VALUE);
    when(directEndPoint.getGetRepresentation()).thenReturn(getRepresentation);
    when(directEndPoint.getPostRepresentation()).thenReturn(postRepresentation);

    Map<org.eclipse.rdf4j.model.Resource, DirectEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(DBEERPEDIA.DOC_ENDPOINT, directEndPoint);
    when(directEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    directEndPointRequestMapper = new DirectEndPointRequestMapper(directEndPointResourceProvider,
        supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner,
        representationRequestHandlerFactory, transactionRequestHandlerFactory);
    representationRequestHandler = new RepresentationRequestHandler(directEndPoint,
        endPointRequestParameterMapper, representationResourceProvider);

    when(representationRequestHandlerFactory.newRepresentationRequestHandler(
        isA(AbstractEndPoint.class))).thenReturn(representationRequestHandler);
    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // Arrange / Act
    DirectEndPointRequestMapper directEndPointRequestMapper =
        new DirectEndPointRequestMapper(directEndPointResourceProvider,
            supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner,
            representationRequestHandlerFactory, transactionRequestHandlerFactory);

    // Assert
    assertThat(directEndPointRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    // Act
    directEndPointRequestMapper.loadDirectEndPoints(httpConfiguration);

    // Assert
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    final ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(httpConfiguration.getResources(), hasSize(2));
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.ORG_HOST
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
    assertThat(resource.getResourceMethods(), hasSize(1));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithoutStage() {
    // Arrange
    directEndPoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DirectEndPoint> endPointMap = new HashMap<>();
    endPointMap.put(directEndPoint.getIdentifier(), directEndPoint);
    when(directEndPointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    directEndPointRequestMapper.loadDirectEndPoints(httpConfiguration);

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
    directEndPointRequestMapper.loadDirectEndPoints(httpConfiguration);

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
    directEndPointRequestMapper.loadDirectEndPoints(httpConfiguration);

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
    directEndPointRequestMapper.loadDirectEndPoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    assertThat(resource.getPath(), equalTo("/" + Stage.PATH_DOMAIN_PARAMETER
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
  }

}
