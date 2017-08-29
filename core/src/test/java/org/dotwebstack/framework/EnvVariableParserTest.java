package org.dotwebstack.framework;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {EnvVariableParser.class})
public class EnvVariableParserTest {

  EnvVariableParser envVariableParser;

  @Before
  public void setUp() throws Exception {

    Map<String, String> env = new HashMap<>();
    env.put("NAME", DBEERPEDIA.BREWERY_DAVO_NAME);

    PowerMockito.mockStatic(System.class);
    when(System.getenv()).thenReturn(env);

    envVariableParser = new EnvVariableParser();
  }

  @Test
  public void parse() throws Exception {
    String input = "${NAME}";

    String output = envVariableParser.parse(input);

    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void parseRdf4jStatement() throws Exception {
    ValueFactory factory = SimpleValueFactory.getInstance();

    IRI subject = DBEERPEDIA.BREWERIES;
    IRI predicate = DBEERPEDIA.NAME;
    Literal object = factory.createLiteral("${NAME}");
    Statement nameStatement = factory.createStatement(subject, predicate, object);

    Statement newStatement = envVariableParser.parse(nameStatement);

    assertThat(newStatement.getSubject(), is(subject));
    assertThat(newStatement.getPredicate(), is(predicate));
    Literal newObject = DBEERPEDIA.BREWERY_DAVO;
    assertThat(newStatement.getObject(), is(newObject));
  }
}
