package org.dotwebstack.framework.frontend.openapi.ldpath;

import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableCollection;
import java.util.UUID;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.eclipse.rdf4j.model.IRI;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class KeepAfterLastFunctionTest extends AbstractFunctionTest {

  private IRI subject;

  private IRI predicate;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    subject = iri("ex", UUID.randomUUID().toString());
    predicate = iri("vocab", UUID.randomUUID().toString());
  }

  @Test
  public void testKeepAfterLastFunction() throws ParseException {
    final String id = "baz";
    final String ldPath =
        String.format("fn:keepAfterLast(<%s>, \"%s\") :: xsd:string", predicate.stringValue(), "/");
    addStatement(repository.getValueFactory().createStatement(subject, predicate, iri("ex", id)));

    final ImmutableCollection<Object> values = evaluateRule(ldPath, subject);

    assertThat(values.size(), Matchers.equalTo(1));
    assertThat(values, Matchers.contains("example.com#baz"));
  }

  @Test
  public void testNoOccurrence() throws ParseException {
    final String value = "stringLiteral";
    final String ldPath =
        String.format("fn:keepAfterLast(<%s>, \"%s\") :: xsd:string", predicate.stringValue(), "/");
    addStatement(repository.getValueFactory().createStatement(subject, predicate,
        repository.getValueFactory().createLiteral(value)));

    final ImmutableCollection<Object> values = evaluateRule(ldPath, subject);

    assertThat(values.size(), Matchers.equalTo(1));
    assertThat(values, Matchers.contains(value));
  }

  @Test
  public void testValidateNumberOfArguments() throws ParseException {
    final String id = "baz";
    final String ldPath =
        String.format("fn:keepAfterLast(<%s>) :: xsd:string", predicate.stringValue());
    addStatement(repository.getValueFactory().createStatement(subject, predicate, iri("ex", id)));

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("LdPath function keepAfterLast requires 2 arguments");

    evaluateRule(ldPath, subject);
  }

}
