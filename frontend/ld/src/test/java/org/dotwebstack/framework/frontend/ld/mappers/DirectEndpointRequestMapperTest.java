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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint.Builder;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpointResourceProvider;
import org.dotwebstack.framework.frontend.ld.handlers.DirectEndpointRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.EndpointRequestParameterMapper;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.RequestHandler;
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
public class DirectEndpointRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Mock
  private DirectEndpoint directEndpoint;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private DirectEndpointResourceProvider directEndpointResourceProvider;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Mock
  private EndpointRequestParameterMapper endpointRequestParameterMapper;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private ServiceRequestHandlerFactory transactionRequestHandlerFactory;

  @Mock
  private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  private RequestHandler<DirectEndpoint> representationRequestHandler;

  private DirectEndpointRequestMapper directEndpointRequestMapper;

  private HttpConfiguration httpConfiguration;

  private Representation getRepresentation;

  private Representation postRepresentation;

  @Before
  public void setUp() {
    getRepresentation = new Representation.RepresentationBuilder(
        DBEERPEDIA.BREWERY_REPRESENTATION).informationProduct(informationProduct).build();
    postRepresentation = new Representation.RepresentationBuilder(
        DBEERPEDIA.BREWERY_REPRESENTATION).informationProduct(informationProduct).build();

    when(directEndpoint.getStage()).thenReturn(stage);
    when(directEndpoint.getStage().getFullPath()).thenReturn(
        "/" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.BASE_PATH.getLabel());
    when(directEndpoint.getPathPattern()).thenReturn(DBEERPEDIA.PATH_PATTERN_VALUE);
    when(directEndpoint.getGetRepresentation()).thenReturn(getRepresentation);
    when(directEndpoint.getPostRepresentation()).thenReturn(postRepresentation);

    Map<org.eclipse.rdf4j.model.Resource, DirectEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(DBEERPEDIA.DOC_ENDPOINT, directEndpoint);
    when(directEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    directEndpointRequestMapper = new DirectEndpointRequestMapper(directEndpointResourceProvider,
        supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner,
        representationRequestHandlerFactory, transactionRequestHandlerFactory);
    representationRequestHandler = new DirectEndpointRequestHandler(directEndpoint,
        endpointRequestParameterMapper, representationResourceProvider);

    when(representationRequestHandlerFactory.newRepresentationRequestHandler(
        isA(DirectEndpoint.class))).thenReturn(representationRequestHandler);
    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // Arrange / Act
    DirectEndpointRequestMapper directEndpointRequestMapper =
        new DirectEndpointRequestMapper(directEndpointResourceProvider,
            supportedWriterMediaTypesScanner, supportedReaderMediaTypesScanner,
            representationRequestHandlerFactory, transactionRequestHandlerFactory);

    // Assert
    assertThat(directEndpointRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    // Act
    directEndpointRequestMapper.loadDirectEndpoints(httpConfiguration);

    // Assert
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    final ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(httpConfiguration.getResources(), hasSize(2));
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.ORG_HOST
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
    assertThat(method.getHttpMethod(), equalTo(HttpMethod.GET));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithoutStage() {
    // Arrange
    directEndpoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DirectEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(directEndpoint.getIdentifier(), directEndpoint);
    when(directEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    directEndpointRequestMapper.loadDirectEndpoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithNullStage() {
    // Arrange
    directEndpoint = new Builder(DBEERPEDIA.DOC_ENDPOINT, DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, DirectEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(directEndpoint.getIdentifier(), directEndpoint);
    when(directEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    directEndpointRequestMapper.loadDirectEndpoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_IgnoreSecondRepresentation_WhenAddedTwice() {
    // Arrange
    when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    DirectEndpoint endPoint = (DirectEndpoint) new Builder(DBEERPEDIA.DOC_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).getRepresentation(getRepresentation).stage(stage).build();
    DirectEndpoint samePathEndpoint = (DirectEndpoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).getRepresentation(getRepresentation).stage(stage).build();
    Map<org.eclipse.rdf4j.model.Resource, DirectEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    endPointMap.put(samePathEndpoint.getIdentifier(), samePathEndpoint);
    when(directEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    directEndpointRequestMapper.loadDirectEndpoints(httpConfiguration);

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
    DirectEndpoint endPoint = (DirectEndpoint) new Builder(DBEERPEDIA.DEFAULT_ENDPOINT,
        DBEERPEDIA.PATH_PATTERN_VALUE).getRepresentation(getRepresentation).stage(stage).build();

    Map<org.eclipse.rdf4j.model.Resource, DirectEndpoint> endPointMap = new HashMap<>();
    endPointMap.put(endPoint.getIdentifier(), endPoint);
    when(directEndpointResourceProvider.getAll()).thenReturn(endPointMap);

    // Act
    directEndpointRequestMapper.loadDirectEndpoints(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    assertThat(resource.getPath(), equalTo("/" + Stage.PATH_DOMAIN_PARAMETER
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
  }

}
