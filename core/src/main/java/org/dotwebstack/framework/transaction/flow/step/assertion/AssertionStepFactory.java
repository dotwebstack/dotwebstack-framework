package org.dotwebstack.framework.transaction.flow.step.assertion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.queryvisitor.FederatedQueryVisitor;
import org.dotwebstack.framework.transaction.flow.step.StepFactory;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssertionStepFactory implements StepFactory {

  private static final Logger LOG = LoggerFactory.getLogger(AssertionStepFactory.class);

  private static final Pattern regexPrefix = Pattern.compile(
      "(prefix)\\b\\s*[a-zA-Z0-9]\\w*\\s*\\:\\s*\\<.*\\>", Pattern.CASE_INSENSITIVE);

  private static final Pattern regexService = Pattern.compile(
      "(service)\\s*[a-zA-Z0-9]*\\w*\\s*\\:\\s*[a-zA-Z0-9]*\\w+", Pattern.CASE_INSENSITIVE);

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private BackendResourceProvider backendResourceProvider;

  @Autowired
  public AssertionStepFactory(@NonNull BackendResourceProvider backendResourceProvider) {
    this.backendResourceProvider = backendResourceProvider;
  }

  @Override
  public boolean supports(@NonNull IRI stepType) {
    return stepType.equals(ELMO.ASSERTION_STEP);
  }

  @Override
  public AssertionStep create(@NonNull Model stepModel, @NonNull Resource identifier) {
    AssertionStep.Builder builder = new AssertionStep.Builder(identifier);
    getObjectString(stepModel, identifier, RDFS.LABEL).ifPresent(builder::label);
    final Optional<String> assertionQuery = getObjectString(stepModel, identifier, ELMO.ASSERT);
    final Optional<String> assertionNotQuery =
        getObjectString(stepModel, identifier, ELMO.ASSERT_NOT);
    builder.assertion(transformQuery(assertionQuery), transformQuery(assertionNotQuery));
    return builder.build();
  }

  private Optional<String> transformQuery(Optional<String> assertionQuery) {
    if (assertionQuery.isPresent()) {
      SPARQLParserFactory sparqlParserFactory = new SPARQLParserFactory();
      QueryParser queryParser = sparqlParserFactory.getParser();

      ParsedQuery parsedQuery =
          queryParser.parseQuery(assertionQuery.get(), ELMO.CONFIG_GRAPHNAME.toString());
      FederatedQueryVisitor visitor = new FederatedQueryVisitor(backendResourceProvider);
      parsedQuery.getTupleExpr().visit(visitor);
    }
    return Optional.empty();
  }

  private Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

}
