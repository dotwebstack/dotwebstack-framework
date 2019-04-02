package org.dotwebstack.framework.frontend.openapi.ldpath;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ExistsFunctionTest extends AbstractFunctionTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Override
  protected LdPathParser<Value> createParserFromString(String program) {
    LdPathParser<Value> parser = super.createParserFromString(program);

    parser.registerFunction(new ExistsFunction<>());

    return parser;
  }

  @Test
  public void testNodeExists() throws ParseException {
    IRI subject = iri("ex", "foo");
    IRI predicate = iri("ex", "bar");
    IRI unpredicated = iri("ex", "qux");
    Literal object = repository.getValueFactory().createLiteral(" \t\n\rbas \t\n\r");

    addStatement(repository.getValueFactory().createStatement(subject, predicate, object));

    String ldPath = String.format("fn:exists(<%s>) :: xsd:boolean", predicate.stringValue());
    Collection<Object> result = evaluateRule(ldPath, subject);

    assertThat(result, hasSize(1));
    assertThat(result, contains(true));

    ldPath = String.format("fn:exists(<%s>) :: xsd:boolean", unpredicated.stringValue());
    result = evaluateRule(ldPath, subject);

    assertThat(result, hasSize(1));
    assertThat(result, contains(false));
  }


  @Test
  public void testNotEnoughArgsExists() throws ParseException {
    String rule = "fn:exists()";
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("LdPath function 'exists' requires 1 argument");

    evaluateRule(rule, null);
  }

  @Test
  public void testMultipleArgsExists() throws ParseException {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("LdPath function 'exists' requires 1 argument");
    IRI subject = iri("ex", "foo");
    IRI predicate = iri("ex", "bar");
    IRI predicate2 = iri("ex", "qux");

    String ldPath = String.format("fn:exists(<%s>, <%s>) :: xsd:boolean", predicate.stringValue(),
        predicate2.stringValue());
    evaluateRule(ldPath, subject);
  }
}
