package org.dotwebstack.framework.frontend.ld;


import static javax.ws.rs.HttpMethod.GET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.AbstractInformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
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
public class LdRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Captor
  private ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private Backend backend;

  @Mock
  private Stage stage;

  @Mock
  private Site site;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private InformationProductResourceProvider informationProductResourceProvider;

  @Mock
  private AbstractInformationProduct abstractInformationProduct;

  @Mock
  private Representation representation;

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

  private LdRequestMapper requestMapper;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    site = new Site.Builder(DBEERPEDIA.BREWERIES)
        .domain(DBEERPEDIA.DOMAIN.stringValue())
        .build();

    stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site)
        .basePath(DBEERPEDIA.BASE_PATH.stringValue())
        .build();

    informationProductResourceProvider =
        new InformationProductResourceProvider(configurationBackend, backendResourceProvider);

    when(backendResourceProvider.get(any())).thenReturn(backend);
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND))));

    informationProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT), eq(null),
        any())).thenReturn(informationProduct);

    representation = new Representation.Builder(DBEERPEDIA.BREWERIES)
        .informationProduct(informationProduct)
        .stage(stage)
        .urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE)
        .build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    requestMapper =
        new LdRequestMapper(representationResourceProvider);
  }

  @Test
  public void constructRequestMapperNotNullTest() {
    // Arrange/Act
    LdRequestMapper requestMapper = new LdRequestMapper(representationResourceProvider);

    // Assert
    assertThat(requestMapper, not(nullValue()));
  }

  @Test
  public void mapRepresentationTest() {
    // Act
    requestMapper.loadRepresentations(httpConfiguration);

    // Arrange
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    ResourceMethod method = resource.getResourceMethods().get(0);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(resource.getPath(),
        equalTo("/" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.BASE_PATH.getLabel()
            + DBEERPEDIA.URL_PATTERN_VALUE));
    assertThat(resource.getResourceMethods(), hasSize(1));
    assertThat(method.getHttpMethod(), equalTo(GET));
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
