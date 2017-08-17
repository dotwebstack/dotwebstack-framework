package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import org.dotwebstack.framework.Registry;
import org.dotwebstack.framework.TestConfigurationBackend;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
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
    addTriples(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, DBEERPEDIA.BACKEND,
        DBEERPEDIA.BREWERIES_LABEL);

    when(registry.getBackend(DBEERPEDIA.BACKEND)).thenReturn(backend);
    when(backend.createSource(any())).thenReturn(backendSource);

    // Act
    informationProductLoader.load();

    // Assert
    verify(registry).registerInformationProduct(productArgumentCaptor.capture());
    assertThat(productArgumentCaptor.getValue().getBackendSource(), equalTo(backendSource));
    assertThat(productArgumentCaptor.getValue().getLabel(),
        equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(productArgumentCaptor.getValue().getIdentifier(),
        equalTo(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT));
  }

  private void addTriples(IRI informationProductIRI, IRI backendIRI, Literal label) {
    Model informationProductTriples = new ModelBuilder().subject(informationProductIRI)
        .add(RDF.TYPE, ELMO.INFORMATION_PRODUCT)
        .add(RDFS.LABEL, label)
        .add(ELMO.BACKEND, backendIRI).build();
    configurationBackend.addModel(informationProductTriples);
  }

  @Test
  public void loadTwoInformationProducts() {
    // Arrange
    addTriples(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, DBEERPEDIA.BACKEND,
        DBEERPEDIA.BREWERIES_LABEL);
    addTriples(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, DBEERPEDIA.SECOND_BACKEND,
        DBEERPEDIA.WINERIES_LABEL);

    when(registry.getBackend(DBEERPEDIA.BACKEND)).thenReturn(backend);
    when(backend.createSource(any())).thenReturn(backendSource);
    when(registry.getBackend(DBEERPEDIA.SECOND_BACKEND)).thenReturn(backend2);
    when(backend2.createSource(any())).thenReturn(backendSource2);

    // Act
    informationProductLoader.load();

    // Assert
    verify(registry, times(2)).registerInformationProduct(productArgumentCaptor.capture());

    InformationProduct informationProduct1 = productArgumentCaptor.getAllValues().get(0);
    assertThat(informationProduct1.getBackendSource(), equalTo(backendSource));
    assertThat(informationProduct1.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(informationProduct1.getIdentifier(),
        equalTo(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT));

    InformationProduct informationProduct2 = productArgumentCaptor.getAllValues().get(1);
    assertThat(informationProduct2.getBackendSource(), equalTo(backendSource2));
    assertThat(informationProduct2.getLabel(), equalTo(DBEERPEDIA.WINERIES_LABEL.stringValue()));
    assertThat(informationProduct2.getIdentifier(), equalTo(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT));
  }
}
