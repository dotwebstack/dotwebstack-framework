package org.dotwebstack.framework.frontend.openapi.ldpath;

import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.StringReader;
import org.apache.marmotta.ldpath.model.fields.FieldMapping;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.dotwebstack.framework.frontend.openapi.entity.backend.Rdf4jRepositoryBackend;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractFunctionTest {

  private static final ImmutableMap<String, String> NAMESPACES =
      ImmutableMap.of("ex", "http://example.com#", "vocab", "http://foo/vocab#", "ro",
          "http://data.informatiehuisruimte.nl/def/ro#");

  protected SailRepository repository;

  protected Rdf4jRepositoryBackend backend;

  @Before
  public void before() {
    repository = new SailRepository(new MemoryStore());
    repository.initialize();
    backend = new Rdf4jRepositoryBackend(repository);
  }

  @After
  public void after() throws RepositoryException {
    repository.shutDown();
  }

  protected static String ns(String prefix) {
    if (!NAMESPACES.containsKey(prefix)) {
      throw new IllegalArgumentException(String.format("Unknown prefix \"%s\".", prefix));
    }

    return NAMESPACES.get(prefix);
  }

  protected IRI iri(String prefix, String local) {
    return repository.getValueFactory().createIRI(ns(prefix) + local);
  }

  protected LdPathParser<Value> createParserFromString(String program) {
    final LdPathParser<Value> parser = new LdPathParser<Value>(backend, new StringReader(program));
    parser.registerFunction(new KeepAfterLastFunction<>());

    assertThat("Could not parse ldPath", parser, Matchers.notNullValue());

    return parser;
  }

  protected ImmutableCollection<Object> evaluateRule(final String ldPath, IRI context)
      throws ParseException {
    final LdPathParser<Value> parser = createParserFromString(ldPath);
    final FieldMapping<Object, Value> rule = parser.parseRule(NAMESPACES);

    return ImmutableList.copyOf(rule.getValues(backend, context));
  }

  protected void addStatement(Statement statement) {
    final SailRepositoryConnection con = repository.getConnection();

    try {
      con.add(statement);
      con.commit();
    } finally {
      con.close();
    }
  }

}
