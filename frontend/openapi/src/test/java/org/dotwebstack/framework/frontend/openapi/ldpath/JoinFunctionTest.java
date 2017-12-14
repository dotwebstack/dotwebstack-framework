package org.dotwebstack.framework.frontend.openapi.ldpath;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JoinFunctionTest extends AbstractFunctionTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private IRI titelSubject;
  private IRI labelPredicate;
  private IRI nummerPredicate;
  private IRI naamPredicate;
  private IRI unknownPredicate;

  @Before
  public void setUp() {
    titelSubject = iri("ex", "titel");
    labelPredicate = iri("ex", "label");

    Literal hoofdstukObject = repository.getValueFactory().createLiteral("Hoofdstuk");

    addStatement(repository.getValueFactory().createStatement(titelSubject, labelPredicate,
        hoofdstukObject));

    nummerPredicate = iri("ex", "nummer");
    Literal threeObject = repository.getValueFactory().createLiteral("3");

    addStatement(
        repository.getValueFactory().createStatement(titelSubject, nummerPredicate, threeObject));

    naamPredicate = iri("ex", "naam");
    Literal beleidskaderObject = repository.getValueFactory().createLiteral("Beleidskader");

    addStatement(repository.getValueFactory().createStatement(titelSubject, naamPredicate,
        beleidskaderObject));

    unknownPredicate = iri("ex", "unknown");
  }

  @Override
  protected LdPathParser<Value> createParserFromString(String program) {
    LdPathParser<Value> parser = super.createParserFromString(program);

    parser.registerFunction(new JoinFunction<>());

    return parser;
  }

  @Test
  public void throwsExceptionIfNoArgumentsAreSupplied() throws ParseException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("LdPath function 'join' requires at least 1 argument");

    String ldPath = String.format("fn:join() :: xsd:string");

    evaluateRule(ldPath, titelSubject);
  }

  @Test
  public void doesNothingIfNoNodeIsSupplied() throws ParseException {
    String ldPath = String.format("fn:join(\"-\") :: xsd:string");

    Collection<Object> result = evaluateRule(ldPath, titelSubject);

    assertThat(result, Matchers.empty());
  }

  @Test
  public void doesNothingWithDelimiterForASingleNode() throws ParseException {
    String ldPath =
        String.format("fn:join(\"-\", <%s>) :: xsd:string", labelPredicate.stringValue());

    Collection<Object> result = evaluateRule(ldPath, titelSubject);

    assertThat(result, contains("Hoofdstuk"));
  }

  @Test
  public void joinsTwoNodesWithDelimiter() throws ParseException {
    String ldPath = String.format("fn:join(\" \", <%s>, <%s>) :: xsd:string",
        labelPredicate.stringValue(), nummerPredicate.stringValue());

    Collection<Object> result = evaluateRule(ldPath, titelSubject);

    assertThat(result, contains("Hoofdstuk 3"));
  }

  @Test
  public void joinsThreeNodesWithDelimiter() throws ParseException {
    String ldPath = String.format("fn:join(\" \", <%s>, <%s>, <%s>) :: xsd:string",
        labelPredicate.stringValue(), nummerPredicate.stringValue(), naamPredicate.stringValue());

    Collection<Object> result = evaluateRule(ldPath, titelSubject);

    assertThat(result, contains("Hoofdstuk 3 Beleidskader"));
  }

  @Test
  public void ignoresEmptyLdPathResult() throws ParseException {
    String ldPath = String.format("fn:join(\" \", <%s>, <%s>, <%s>) :: xsd:string",
        labelPredicate.stringValue(), unknownPredicate.stringValue(), naamPredicate.stringValue());

    Collection<Object> result = evaluateRule(ldPath, titelSubject);

    assertThat(result, contains("Hoofdstuk Beleidskader"));
  }

}
