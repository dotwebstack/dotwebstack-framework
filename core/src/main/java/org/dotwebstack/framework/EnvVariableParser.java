package org.dotwebstack.framework;

import java.util.Map;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Service;

@Service
public class EnvVariableParser {

  private Map<String, String> envVars;

  private StrSubstitutor strSubstitutor;

  public EnvVariableParser() {
    envVars = System.getenv();
    strSubstitutor = new StrSubstitutor(envVars);
  }

  public String parse(String input) {
    String output = strSubstitutor.replace(input);

    return output;
  }

  public Statement parse(Statement statement) {
    ValueFactory factory = SimpleValueFactory.getInstance();

    IRI subject = factory.createIRI(parse(statement.getSubject().stringValue()));
    IRI predicate = factory.createIRI(parse(statement.getPredicate().stringValue()));
    Literal object = factory.createLiteral(parse(statement.getObject().stringValue()));

    Statement updatedStatement = factory.createStatement(subject, predicate, object);

    return updatedStatement;
  }

}
