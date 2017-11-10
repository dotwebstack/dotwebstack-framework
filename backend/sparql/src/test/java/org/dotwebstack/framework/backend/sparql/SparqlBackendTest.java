package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private SPARQLRepository repository;

  @Mock
  private SparqlBackendInformationProductFactory informationProductFactory;

  @Mock
  private Model model;

  @Mock
  private IRI identifier;

  @Test
  public void constructor_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackend.Builder(null, repository, informationProductFactory);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRepository() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackend.Builder(DBEERPEDIA.BACKEND, null, informationProductFactory);
  }

  @Test
  public void constructor_ThrowsException_WithMissingInformationProductFactory() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackend.Builder(DBEERPEDIA.BACKEND, repository, null);
  }

  @Test
  public void build_CreatesBackend_WithCorrectData() {
    // Act
    SparqlBackend backend = new SparqlBackend.Builder(DBEERPEDIA.BACKEND, repository,
        informationProductFactory).build();

    // Assert
    assertThat(backend.getIdentifier(), equalTo(DBEERPEDIA.BACKEND));
    assertThat(backend.getRepository(), equalTo(repository));
  }

  @Test
  public void getConnection_ReusesConnection_WhenCalledTwice() {
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
  public void createInformationProduct_CreatesInformationProduct_WithValidData() {
    // Arrange
    SparqlBackend backend = new SparqlBackend.Builder(DBEERPEDIA.BACKEND, repository,
        informationProductFactory).build();

    InformationProduct informationProductMock = mock(InformationProduct.class);

    Parameter<?> requiredParameterMock = mock(Parameter.class);
    Parameter<?> optionalParameterMock = mock(Parameter.class);

    when(informationProductFactory.create(identifier, DBEERPEDIA.BREWERIES_LABEL.stringValue(),
        backend, ImmutableList.of(requiredParameterMock, optionalParameterMock), model)).thenReturn(
            informationProductMock);

    // Act
    InformationProduct result =
        backend.createInformationProduct(identifier, DBEERPEDIA.BREWERIES_LABEL.stringValue(),
            ImmutableList.of(requiredParameterMock, optionalParameterMock), model);

    // Assert
    assertThat(result, equalTo(informationProductMock));
  }

}
