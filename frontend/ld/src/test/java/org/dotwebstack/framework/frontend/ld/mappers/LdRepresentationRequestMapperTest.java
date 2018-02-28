package org.dotwebstack.framework.frontend.ld.mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNot.not;
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
import org.dotwebstack.framework.frontend.ld.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.handlers.RepresentationRequestParameterMapper;
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
public class LdRepresentationRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Mock
  private Site site;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private Representation representation;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private SupportedMediaTypesScanner supportedMediaTypesScanner;

  @Mock
  private LdRepresentationRequestMapper ldRepresentationRequestMapper;

  @Mock
  private RepresentationRequestParameterMapper representationRequestParameterMapper;

  private RepresentationRequestHandler representationRequestHandler;

  @Mock
  private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() {
    site = new Site.Builder(DBEERPEDIA.BREWERIES).domain(DBEERPEDIA.DOMAIN.stringValue()).build();

    stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();

    representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
        informationProduct).stage(stage).appliesTo(DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    representationRequestHandler =
        new RepresentationRequestHandler(representation, representationRequestParameterMapper);
    ldRepresentationRequestMapper =
        new LdRepresentationRequestMapper(representationResourceProvider,
            supportedMediaTypesScanner, representationRequestHandlerFactory);
    when(representationRequestHandlerFactory.newRepresentationRequestHandler(
        isA(Representation.class))).thenReturn(representationRequestHandler);

    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // Arrange / Act
    LdRepresentationRequestMapper ldRepresentationRequestMapper =
        new LdRepresentationRequestMapper(representationResourceProvider,
            supportedMediaTypesScanner, representationRequestHandlerFactory);

    // Assert
    assertThat(ldRepresentationRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // Arrange
    when(supportedMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    // Act
    ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);

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
    representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
        informationProduct).appliesTo(DBEERPEDIA.PATH_PATTERN_VALUE).build();
    Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);
    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    // Act
    ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithNullStage() {
    // Arrange
    representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
        informationProduct).appliesTo(DBEERPEDIA.PATH_PATTERN_VALUE).stage(null).build();
    Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);
    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    // Act
    ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_IgnoreSecondRepresentation_WhenAddedTwice() {
    // Arrange
    when(supportedMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
            informationProduct).appliesTo(DBEERPEDIA.PATH_PATTERN_VALUE).stage(stage).build();
    Representation samePathRepresentation =
        new Representation.Builder(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION).informationProduct(
            informationProduct).appliesTo(DBEERPEDIA.PATH_PATTERN_VALUE).stage(stage).build();
    Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);
    representationMap.put(samePathRepresentation.getIdentifier(), samePathRepresentation);
    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    // Act
    ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void loadRepresentations_UsesPathDomainParameter_WithMatchAllDomain() {
    // Arrange
    when(supportedMediaTypesScanner.getMediaTypes(any())).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    Site site = new Site.Builder(DBEERPEDIA.BREWERIES).build();
    Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();
    Representation representation =
        new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
            informationProduct).appliesTo(DBEERPEDIA.PATH_PATTERN_VALUE).stage(stage).build();

    Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);
    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    // Act
    ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    assertThat(resource.getPath(), equalTo("/" + Stage.PATH_DOMAIN_PARAMETER
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));

  }

}
