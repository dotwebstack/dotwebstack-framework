package org.dotwebstack.framework.frontend.openapi.entity.backend;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Rdf4jRepositoryBackendTest {

  private Rdf4jRepositoryBackend backend;

  @Mock
  private Repository repository;

  @Before
  public void before() {
    when(repository.getValueFactory()).thenReturn(SimpleValueFactory.getInstance());
    backend = new Rdf4jRepositoryBackend(repository);
  }

  @Test
  public void testStringLiteral() {
    Literal literal = backend.createLiteral("test");
    assertEquals("test", literal.stringValue());
  }

  @Test
  public void testStringIri() {
    IRI iri = backend.createURI("http://www.test.nl");
    assertEquals("www.test.nl", iri.getLocalName());
  }

  @Test
  public void testListObjects() throws Exception {
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
    Collection<Value> values = backend.listObjects(subject, prop);
    assertThat(values, hasSize(1));
    assertThat(values.iterator().next().stringValue(), is("test"));
  }

  @Test
  public void testCreateLiteral() throws Exception {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    Literal literal = backend.createLiteralInternal(valueFactory, "test");
    assertThat(literal, is(SimpleValueFactory.getInstance().createLiteral("test")));
  }


  @Test
  public void testListSubjects() throws Exception {
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
    Collection<Value> values = backend.listSubjects(subject, prop);
    assertThat(values, hasSize(1));
    assertThat(values.iterator().next(),
        is(SimpleValueFactory.getInstance().createIRI("http://www.test.nl#sub")));
  }

  @Test
  public void testCreateUri() throws Exception {

    IRI iri = backend.createURI("http://www.test.nl");
    assertThat(iri, is(SimpleValueFactory.getInstance().createIRI("http://www.test.nl")));

  }

  @Test
  public void testcreateLiteral() throws Exception {

    Literal literal = backend.createLiteral("http://www.test.nl", Locale.CANADA,
        URI.create("http://www.test.nl"));
    assertThat(literal, is(SimpleValueFactory.getInstance().createLiteral("http://www.test.nl",
        SimpleValueFactory.getInstance().createIRI("http://www.test.nl"))));

  }



}
