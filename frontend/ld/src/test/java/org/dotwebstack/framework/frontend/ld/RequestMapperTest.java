package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
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
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.glassfish.jersey.server.model.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
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
  private HttpConfiguration httpConfiguration = new HttpConfiguration();

  private RequestMapper requestMapper;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

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

    representation = new Representation.Builder(DBEERPEDIA.BREWERIES,
        DBEERPEDIA.URL_PATTERN.stringValue())
        .informationProduct(informationProduct)
        .stage(stage)
        .build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    requestMapper =
        new RequestMapper(representationResourceProvider, httpConfiguration);
  }

  @Test
  public void dubbleRequestMappingRepresentation() {
    //temp();
    int numbers = representationResourceProvider.getAll().size();
    System.out.println("got " + numbers + " representations");

    requestMapper.loadRepresenations();

    verify(httpConfiguration).registerResources(resourceCaptor.capture());
    Resource resource = resourceCaptor.getValue();
    assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.OPENAPI_HOST + "/breweries"));
  }

  private void temp() {

    /*// Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND))));

    // Act
    informationProductResourceProvider.loadResources();*/

   /* // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE),
            valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN))));

    // Act
    siteResourceProvider.loadResources();*/

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList
            .of(// representation
                valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, RDF.TYPE,
                    ELMO.REPRESENTATION),
                valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION,
                    ELMO.INFORMATION_PRODUCT_PROP,
                    DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
                valueFactory
                    .createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, ELMO.URL_PATTERN,
                        DBEERPEDIA.URL_PATTERN),
                valueFactory
                    .createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, ELMO.STAGE_PROP,
                        DBEERPEDIA.STAGE),
                // stage
                valueFactory.createStatement(ELMO.STAGE_PROP, RDF.TYPE, ELMO.STAGE),
                valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.SITE_PROP, DBEERPEDIA.SITE),
                valueFactory
                    .createStatement(DBEERPEDIA.STAGE, ELMO.BASE_PATH, DBEERPEDIA.BASE_PATH),
                // information product
                valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                    ELMO.INFORMATION_PRODUCT),
                valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                    ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
                // site
                valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE),
                valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN)
            )));

    // Act
    representationResourceProvider.loadResources();
  }

  private void mockRepresentation() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP,
                DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, ELMO.URL_PATTERN,
                DBEERPEDIA.URL_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, ELMO.STAGE_PROP,
                DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, ELMO.URL_PATTERN,
                DBEERPEDIA.URL_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, ELMO.STAGE_PROP,
                DBEERPEDIA.STAGE),
            valueFactory
                .createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, RDF.TYPE, ELMO.STAGE),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, ELMO.SITE_PROP,
                DBEERPEDIA.SITE),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, ELMO.BASE_PATH,
                DBEERPEDIA.BASE_PATH),
            valueFactory
                .createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, RDF.TYPE, ELMO.SITE),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_LIST_REPRESENTATION, ELMO.DOMAIN,
                DBEERPEDIA.DOMAIN)
        )));

    // Act
  }

}
