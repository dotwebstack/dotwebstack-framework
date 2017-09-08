package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendTest {

  @Mock
  private SPARQLRepository repository;

  @Mock
  private SparqlBackendInformationProductFactory informationProductFactory;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private Model model;

  @Test
  public void builder() {
    // Act
    SparqlBackend backend = new SparqlBackend.Builder(DBEERPEDIA.BACKEND, repository,
        informationProductFactory).build();

    // Assert
    assertThat(backend.getIdentifier(), equalTo(DBEERPEDIA.BACKEND));
    assertThat(backend.getRepository(), equalTo(repository));
  }

  @Test
  public void reuseConnection() {
    // Arrange
    SparqlBackend backend = new SparqlBackend.Builder(DBEERPEDIA.BACKEND, repository,
        informationProductFactory).build();
    when(repository.getConnection()).thenReturn(mock(RepositoryConnection.class));

    // Act
    RepositoryConnection repositoryConnection1 = backend.getConnection();
    RepositoryConnection repositoryConnection2 = backend.getConnection();

    // Assert
    assertThat(repositoryConnection1, notNullValue());
    assertThat(repositoryConnection1, equalTo(repositoryConnection2));
    verify(repository).getConnection();
  }

  @Test
  public void decorateInformationProduct() {
    // Arrange
    SparqlBackend backend = new SparqlBackend.Builder(DBEERPEDIA.BACKEND, repository,
        informationProductFactory).build();

    InformationProduct informationProductMock = mock(InformationProduct.class);
    when(informationProductFactory.create(this.informationProduct, backend, model)).thenReturn(
        informationProductMock);

    // Act
    InformationProduct result = backend.decorate(informationProduct, model);

    // Assert
    assertThat(result, equalTo(informationProductMock));
  }

}
