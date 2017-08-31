package org.dotwebstack.framework.frontend.ld;

import static org.mockito.Mockito.when;

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

  Stage stage;

  Site site;

  InformationProduct informationProduct;

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

  private RequestMapper requestMapper;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Mock
  private HttpConfiguration http;

  @Before
  public void setUp() {
    http = new HttpConfiguration();

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
        DBEERPEDIA.URL_PATTERN_VALUE)
        .informationProduct(informationProduct)
        .stage(stage)
        .build();
    Representation representation1 = new Representation.Builder(DBEERPEDIA.BREWERY_REPRESENTATION,
        DBEERPEDIA.URL_PATTERN_VALUE)
        .informationProduct(informationProduct)
        .stage(stage)
        .build();
    Map<IRI, Representation> representationMap = new HashMap<>();
    representationMap.put(representation.getIdentifier(), representation);
    representationMap.put(representation1.getIdentifier(), representation1);

    when(representationResourceProvider.getAll()).thenReturn(representationMap);

    requestMapper =
        new RequestMapper(representationResourceProvider, http);
  }

  @Test
  public void dubbleRequestMappingRepresentation() {

    int numbers = representationResourceProvider.getAll().size();
    System.out.println("got " + numbers + " representations");

    requestMapper.loadRepresenations();
  }
}
