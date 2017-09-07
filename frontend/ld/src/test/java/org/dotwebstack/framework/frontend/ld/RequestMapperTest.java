package org.dotwebstack.framework.frontend.ld;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest(HttpConfiguration.class)
@RunWith(PowerMockRunner.class)
public class RequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Captor
  ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  BackendSource backendSource;

  @Mock
  Stage stage;

  @Mock
  Site site;

  @Mock
  InformationProduct informationProduct;

  @Mock
  Representation representation;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Spy
  private HttpConfiguration httpConfiguration = new HttpConfiguration(ImmutableList.of());

  private RequestMapper requestMapper;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    site = new Site.Builder(DBEERPEDIA.BREWERIES)
        .domain(DBEERPEDIA.DOMAIN.stringValue())
        .build();

    stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site)
        .basePath(DBEERPEDIA.BASE_PATH.stringValue())
        .build();

    informationProduct = new InformationProduct.Builder(DBEERPEDIA.BREWERIES,
        backendSource)
        .label(DBEERPEDIA.BREWERIES_LABEL.stringValue())
        .build();

    representation = new Representation.Builder(DBEERPEDIA.BREWERIES)
        .informationProduct(informationProduct)
        .stage(stage)
        .urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE)
        .build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    requestMapper =
        new RequestMapper(representationResourceProvider);
  }

  @Test
  public void constructRequestMapperTest() {
    new RequestMapper(representationResourceProvider);
  }

  @Test
  public void mapRepresentationTest() {
    // Act
    requestMapper.loadRepresentations(httpConfiguration);

    // Arrange
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(resource.getPath(),
        equalTo("/" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.BASE_PATH.getLabel()
            + DBEERPEDIA.URL_PATTERN_VALUE));
    assertThat(resource.getResourceMethods(), hasSize(1));

    // Arrange
    ResourceMethod method = resource.getResourceMethods().get(0);
    // Assert
    assertThat(method.getHttpMethod(), equalTo("GET"));
  }

  @Test
  public void mapRepresentationWithoutStageTest() {
    // Arrange
    representation = new Representation.Builder(DBEERPEDIA.BREWERIES)
        .informationProduct(informationProduct)
        .urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE)
        .build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    // Act
    requestMapper.loadRepresentations(httpConfiguration);
    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void mapRepresentationWithNullStageTest() {
    // Arrange
    representation = new Representation.Builder(DBEERPEDIA.BREWERIES)
        .informationProduct(informationProduct)
        .urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE)
        .stage(null)
        .build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    // Act
    requestMapper.loadRepresentations(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

}
