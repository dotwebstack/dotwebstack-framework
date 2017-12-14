package org.dotwebstack.framework.frontend.openapi.entity.backend;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryLockedException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Rdf4jRepositoryBackendTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Rdf4jRepositoryBackend backend;
  @Mock
  private Repository repository;

  @Before
  public void before() {
    when(repository.getValueFactory()).thenReturn(SimpleValueFactory.getInstance());
    backend = new Rdf4jRepositoryBackend(repository);
  }

  @Test
  public void createLiteral_GetStringValue_KeepStringValue() {
    // Act
    Literal literal = backend.createLiteral("test");
    // Assert
    assertEquals("test", literal.stringValue());
  }

  @Test
  public void createUri_GetLocalName_KeepStringValue() {
    // Act
    IRI iri = backend.createURI("http://www.test.nl");
    // Assert
    assertEquals("www.test.nl", iri.getLocalName());
  }

  @Test
  public void listObjects_NoDuplicates_WhenSubjectAndPropSimilar() throws Exception {
    // Arrange
    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);
    when(repository.getConnection()).thenReturn(repositoryConnection);
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    when(repositoryConnection.getValueFactory()).thenReturn(valueFactory);
    RepositoryResult queryResult = mock(RepositoryResult.class);

    Model model =
        new ModelBuilder().add("http://www.test.nl#sub", "http://www.test.nl#pred", "test").build();
    when(queryResult.hasNext()).thenReturn(true).thenReturn(false);
    when(queryResult.next()).thenReturn(model.stream().findFirst().get());
    when(
        repositoryConnection.getStatements(any(), any(), isNull(), anyBoolean(), any())).thenReturn(
            queryResult);
    Value subject = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    Value prop = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    // Act
    Collection<Value> values = backend.listObjects(subject, prop);
    // Assert
    assertThat(values, hasSize(1));
    assertThat(values.iterator().next().stringValue(), is("test"));
  }

  @Test
  public void listSubjects_ListOnlySubjects_InMixedModel() throws Exception {
    // Arrange
    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);
    when(repository.getConnection()).thenReturn(repositoryConnection);
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    when(repositoryConnection.getValueFactory()).thenReturn(valueFactory);
    RepositoryResult queryResult = mock(RepositoryResult.class);

    Model model =
        new ModelBuilder().add("http://www.test.nl#sub", "http://www.test.nl#pred", "test").build();
    when(queryResult.hasNext()).thenReturn(true).thenReturn(false);
    when(queryResult.next()).thenReturn(model.stream().findFirst().get());
    when(
        repositoryConnection.getStatements(isNull(), any(), any(), anyBoolean(), any())).thenReturn(
            queryResult);
    Value subject = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    Value prop = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    // Act
    Collection<Value> values = backend.listSubjects(subject, prop);
    // Assert
    assertThat(values, hasSize(1));
    assertThat(values.iterator().next(),
        is(SimpleValueFactory.getInstance().createIRI("http://www.test.nl#sub")));
  }

  @Test
  public void listSubjects_ThrowsExeption_ForIllegalProperty() {
    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Property needs to be a URI node (property type: bNode).");

    // Arrange
    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);
    when(repository.getConnection()).thenReturn(repositoryConnection);
    Value object = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    Value prop = SimpleValueFactory.getInstance().createBNode();

    // Act
    backend.listSubjects(prop, object);
  }

  @Test
  public void listSubjects_ThrowsExeption_QueryingRepositoryFailed() {
    // Assert
    thrown.expect(Rdf4jBackendRuntimeException.class);
    thrown.expectMessage("Error while querying RDF4J repository.");
    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);

    // Arrange
    when(repository.getConnection()).thenReturn(repositoryConnection);
    Exception dummy = new Exception("x");

    doThrow(new RepositoryLockedException("a", "b", "c", dummy)).when(repositoryConnection).begin();
    Value object = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    Value prop = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");

    // Act
    backend.listSubjects(prop, object);
  }

  @Test
  public void createUri_ReturnsUri_WhenCreated() throws Exception {
    // Act
    IRI iri = backend.createURI("http://www.test.nl");

    // Assert
    assertThat(iri, is(SimpleValueFactory.getInstance().createIRI("http://www.test.nl")));
  }

  @Test
  public void createLiteral_ReturnsLiteral_WhenCreatedWithLocaleWithoutTypeWithoutLanguage()
      throws Exception {
    // Act
    Literal literal = backend.createLiteral("http://www.test.nl", null, null);
    // Assert
    assertThat(literal, is(SimpleValueFactory.getInstance().createLiteral("http://www.test.nl")));
    assertEquals("http://www.w3.org/2001/XMLSchema#string", literal.getDatatype().toString());
    assertEquals(false, literal.getLanguage().isPresent());

  }

  /**
   * Language only for type http://www.w3.org/1999/02/22-rdf-syntax-ns#langString see:
   * https://www.w3.org/TR/rdf11-concepts/#section-Graph-Literal
   *
   */
  @Test
  public void createLiteral_ReturnsLiteral_WhenCreatedWithLocaleWithoutType() throws Exception {
    // Act
    Literal literal = backend.createLiteral("http://www.test.nl", Locale.CANADA, null);
    // Assert
    assertThat(literal,
        is(SimpleValueFactory.getInstance().createLiteral("http://www.test.nl", "en")));
    assertEquals(Locale.CANADA.getLanguage(), literal.getLanguage().get());
    assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString",
        literal.getDatatype().toString());

  }

  /**
   * Language only for type http://www.w3.org/1999/02/22-rdf-syntax-ns#langString see:
   * https://www.w3.org/TR/rdf11-concepts/#section-Graph-Literal
   *
   */
  @Test
  public void createLiteral_ReturnsLiteral_WhenCreatedWithLocaleWithTypeLanguageIgnored()
      throws Exception {
    // Act
    Literal literal = backend.createLiteral("http://www.test.nl", Locale.CANADA,
        URI.create("http://www.test.nl"));
    // Assert
    assertEquals(false, literal.getLanguage().isPresent());
    assertEquals("http://www.test.nl", literal.getDatatype().toString());
  }

  @Test
  public void createLiteral_ReturnsLiteral_WhenCreatedEithoutTypeAndLanguage() throws Exception {
    // Act
    Literal literal = backend.createLiteral("http://www.test.nl", null, null);
    // Assert
    assertEquals(false, literal.getLanguage().isPresent());
    assertEquals("http://www.w3.org/2001/XMLSchema#string", literal.getDatatype().toString());
  }

  @Test
  public void listObjects_ThrowsExeption_ForIllegalSubject() {
    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Subject needs to be a URI or blank node, property a URI node"
        + " (types: [subject: URI, property: bNode]).");

    // Arrange
    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);
    when(repository.getConnection()).thenReturn(repositoryConnection);
    Value subject = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    Value prop = SimpleValueFactory.getInstance().createBNode();

    // Act
    backend.listObjects(subject, prop);
  }

  @Test
  public void listObjects_ThrowsExeption_QueryingRepositoryFailed() {
    // Assert
    thrown.expect(Rdf4jBackendRuntimeException.class);
    thrown.expectMessage("Error while querying RDF4J repository.");

    // Arrange
    RepositoryConnection repositoryConnection = mock(RepositoryConnection.class);

    when(repository.getConnection()).thenReturn(repositoryConnection);
    Exception dummy = new Exception("x");

    doThrow(new RepositoryLockedException("a", "b", "c", dummy)).when(repositoryConnection).begin();
    Value subject = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    Value prop = SimpleValueFactory.getInstance().createIRI("http://www.test.nl#test");
    // Act
    backend.listObjects(subject, prop);
  }
}
