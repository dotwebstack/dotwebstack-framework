package org.dotwebstack.framework.frontend.ld;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.site.SiteResourceProvider;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
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
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private InformationProductResourceProvider informationProductResourceProvider;

  @Mock
  private StageResourceProvider stageResourceProvider;

  @Mock
  private SiteResourceProvider siteResourceProvider;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private HttpConfiguration httpConfiguration;

  private RequestMapper requestMapper;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {

    representationResourceProvider = new RepresentationResourceProvider(configurationBackend,
        informationProductResourceProvider, stageResourceProvider);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(informationProductResourceProvider.get(any())).thenReturn(informationProduct);
    when(stageResourceProvider.get(any())).thenReturn(stage);

    requestMapper =
        new RequestMapper(representationResourceProvider, httpConfiguration);
  }

  @Test
  public void dubbleRequestMappingRepresentation() {
    temp();

    int numbers = representationResourceProvider.getAll().size();
    System.out.println("got " + numbers + " representations");

    requestMapper.loadRepresenations();
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
    informationProductResourceProvider.loadResources();

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE),
            valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN))));

    // Act
    siteResourceProvider.loadResources();

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.STAGE, RDF.TYPE, ELMO.STAGE),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.SITE_PROP, DBEERPEDIA.SITE),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.BASE_PATH, DBEERPEDIA.BASE_PATH))));

    // Act
    stageResourceProvider.loadResources();
*/
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList
            .of(valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, RDF.TYPE,
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
                valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.SITE_PROP, DBEERPEDIA.SITE),
                valueFactory
                    .createStatement(DBEERPEDIA.STAGE, ELMO.BASE_PATH, DBEERPEDIA.BASE_PATH),

                valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                    ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
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
    siteResourceProvider.loadResources();
    informationProductResourceProvider.loadResources();
    stageResourceProvider.loadResources();
    representationResourceProvider.loadResources();
  }

}
