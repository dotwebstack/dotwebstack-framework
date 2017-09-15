package org.dotwebstack.framework.frontend.ld;


import static javax.ws.rs.HttpMethod.GET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest(HttpConfiguration.class)
@RunWith(PowerMockRunner.class)
public class LdRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

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
  private Representation representation;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private SupportedMediaTypesScanner supportedMediaTypesScanner;

  private LdRequestMapper requestMapper;

  private HttpConfiguration httpConfiguration;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    site = new Site.Builder(DBEERPEDIA.BREWERIES).domain(DBEERPEDIA.DOMAIN.stringValue()).build();

    stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();

    when(backendResourceProvider.get(any())).thenReturn(backend);
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND))));

    when(informationProduct.getResultType()).thenReturn(ResultType.GRAPH);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT), eq(null),
        any())).thenReturn(informationProduct);

    representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
        informationProduct).stage(stage).urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE).build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    when(supportedMediaTypesScanner.getSparqlProviders()).thenReturn(new ArrayList<>());

    requestMapper = new LdRequestMapper(representationResourceProvider, supportedMediaTypesScanner);

    httpConfiguration = new HttpConfiguration(ImmutableList.of(), supportedMediaTypesScanner);
  }

  @Test
  public void constructRequestMapperNotNullTest() {
    // Arrange/Act
    LdRequestMapper requestMapper =
        new LdRequestMapper(representationResourceProvider, supportedMediaTypesScanner);

    // Assert
    assertThat(requestMapper, not(nullValue()));
  }

  @Test
  public void mapRepresentationTest() {
    // Arrange
    when(supportedMediaTypesScanner.getMediaTypes(ResultType.GRAPH)).thenReturn(
        new MediaType[] {MediaType.valueOf("text/turtle")});

    // Act
    requestMapper.loadRepresentations(httpConfiguration);

    // Assert
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.ORG_HOST
        + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.URL_PATTERN_VALUE));
    assertThat(resource.getResourceMethods(), hasSize(1));
    assertThat(method.getHttpMethod(), equalTo(GET));
  }

  @Test
  public void mapRepresentationWithoutStageTest() {
    // Arrange
    representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
        informationProduct).urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE).build();
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
    representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
        informationProduct).urlPatterns(DBEERPEDIA.URL_PATTERN_VALUE).stage(null).build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);
    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    // Act
    requestMapper.loadRepresentations(httpConfiguration);

    // Assert
    assertThat(httpConfiguration.getResources(), hasSize(0));
  }

}
