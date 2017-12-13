package org.dotwebstack.framework.frontend.openapi.ldpath;

import static org.hamcrest.Matchers.contains;
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

public class TrimFunctionTest extends AbstractFunctionTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Override
  protected LdPathParser<Value> createParserFromString(String program) {
    LdPathParser<Value> parser = super.createParserFromString(program);

    parser.registerFunction(new TrimFunction<>());

    return parser;
  }

  @Test
  public void trimsNode() throws ParseException {
    IRI subject = iri("ex", "foo");
    IRI predicate = iri("ex", "bar");
    Literal object = repository.getValueFactory().createLiteral(" \t\n\rbas \t\n\r");

    addStatement(repository.getValueFactory().createStatement(subject, predicate, object));

    String ldPath = String.format("fn:trim(<%s>) :: xsd:string", predicate.stringValue());
    Collection<Object> result = evaluateRule(ldPath, subject);

    assertThat(result, contains("bas"));
  }

  @Test
  public void testNotEnoughArgs() throws ParseException {
    String rule = "fn:trim()";
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("LdPath function 'trim' requires 1 argument");

    evaluateRule(rule, null);
  }
}
