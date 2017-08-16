package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import org.dotwebstack.framework.InformationProduct;
import org.dotwebstack.framework.Registry;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InformationProductLoaderQueryTest {

  @Mock
  private Registry registry;

  @Mock
  private Backend backend, backend2;

  @Mock
  private BackendSource backendSource, backendSource2;

  @Captor
  private ArgumentCaptor<InformationProduct> productArgumentCaptor;

  private InformationProductLoader informationProductLoader;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private TestConfigurationBackend configurationBackend;

  @Before
  public void setUp() throws IOException {
    configurationBackend = new TestConfigurationBackend();
    informationProductLoader = new InformationProductLoader(registry,
        configurationBackend);
  }

  @Test
  public void loadInformationProduct() {
    // Arrange
    IRI informationProductIRI = valueFactory.createIRI("http://example.org/InformationProduct");
    IRI backendIRI = valueFactory.createIRI("http://example.org/myBackend");
    addTriples(informationProductIRI, backendIRI, "myLabel");

    when(registry.getBackend(backendIRI)).thenReturn(backend);
    when(backend.createSource(any())).thenReturn(backendSource);

    // Act
    informationProductLoader.load();

    // Assert
    verify(registry).registerInformationProduct(productArgumentCaptor.capture());
    assertThat(productArgumentCaptor.getValue().getBackendSource(), equalTo(backendSource));
    assertThat(productArgumentCaptor.getValue().getLabel(), equalTo("myLabel"));
    assertThat(productArgumentCaptor.getValue().getIdentifier(), equalTo(informationProductIRI));
  }

  private void addTriples(IRI informationProductIRI, IRI backendIRI, String label) {
    Model informationProductTriples = new ModelBuilder().subject(informationProductIRI)
        .add(RDF.TYPE, ELMO.INFORMATION_PRODUCT)
        .add(RDFS.LABEL, label)
        .add(ELMO.BACKEND, backendIRI).build();
    configurationBackend.addModel(informationProductTriples);
  }


  @Test
  public void loadTwoInformationProducts() {
    // Arrange
    IRI informationProductIRI1 = valueFactory.createIRI("http://example.org/InformationProduct1");
    IRI informationProductIRI2 = valueFactory.createIRI("http://example.org/InformationProduct2");
    IRI backendIRI1 = valueFactory.createIRI("http://example.org/myBackend1");
    IRI backendIRI2 = valueFactory.createIRI("http://example.org/myBackend2");
    addTriples(informationProductIRI1, backendIRI1, "myLabel1");
    addTriples(informationProductIRI2, backendIRI2, "myLabel2");

    when(registry.getBackend(backendIRI1)).thenReturn(backend);
    when(backend.createSource(any())).thenReturn(backendSource);
    when(registry.getBackend(backendIRI2)).thenReturn(backend2);
    when(backend2.createSource(any())).thenReturn(backendSource2);

    // Act
    informationProductLoader.load();

    // Assert
    verify(registry, times(2)).registerInformationProduct(productArgumentCaptor.capture());

    InformationProduct informationProduct1 = productArgumentCaptor.getAllValues().get(0);
    assertThat(informationProduct1.getBackendSource(), equalTo(backendSource));
    assertThat(informationProduct1.getLabel(), equalTo("myLabel1"));
    assertThat(informationProduct1.getIdentifier(), equalTo(informationProductIRI1));

    InformationProduct informationProduct2 = productArgumentCaptor.getAllValues().get(1);
    assertThat(informationProduct2.getBackendSource(), equalTo(backendSource2));
    assertThat(informationProduct2.getLabel(), equalTo("myLabel2"));
    assertThat(informationProduct2.getIdentifier(), equalTo(informationProductIRI2));
  }

  public class TestConfigurationBackend implements ConfigurationBackend {

    private SailRepository sailRepository;

    public TestConfigurationBackend() throws IOException {
      this.initialize();
    }

    @Override
    public void initialize() throws IOException {
      MemoryStore sail = new MemoryStore();
      //sail.setPersist(true);

      sailRepository = new SailRepository(sail);
      sailRepository.initialize();

      clearAllData();
    }

    private void clearAllData() {
      try(RepositoryConnection connection = sailRepository.getConnection()) {
        connection.clear();
      }
    }

    @Override
    public SailRepository getRepository() {
      return sailRepository;
    }

    public void addModel(Model model) {
      try(RepositoryConnection connection = sailRepository.getConnection()) {
        connection.add(model);
        connection.commit();
      }
    }
  }
}
