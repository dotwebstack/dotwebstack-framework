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
import org.dotwebstack.framework.config.ConfigurationException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssertionStepFactory implements StepFactory {

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
      if (!visitor.getReplaceableBackends().isEmpty()) {
        return Optional.of(setBackendUri(assertionQuery.get(), visitor.getReplaceableBackends()));
      }
    }
    return assertionQuery;
  }

  private String setBackendUri(String query, Map<Resource, Backend> backends) {
    String transformedQuery = "";
    Matcher serviceMatcher = regexService.matcher(query);
    Map<String, IRI> prefixesMap = getPrefixesMap(query);
    while (serviceMatcher.find()) {
      Pair<String, String> servicePair = getPair(serviceMatcher.toMatchResult().group());
      StringBuilder backendKey = new StringBuilder();
      backendKey.append(prefixesMap.get(servicePair.getKey()));
      backendKey.append(servicePair.getValue());
      String replaceString = "SERVICE <"
          + backends.get(valueFactory.createIRI(backendKey.toString())).getEndpoint().stringValue()
          + ">";
      transformedQuery = query.replace(serviceMatcher.toMatchResult().group(), replaceString);
      query = transformedQuery;
    }
    return transformedQuery;
  }

  private Map<String, IRI> getPrefixesMap(String query) {
    Map<String, IRI> prefixesMap = new HashMap<>();
    Matcher matcher = regexPrefix.matcher(query);
    while (matcher.find()) {
      Pair<String, String> pair = getPair(matcher.toMatchResult().group());
      prefixesMap.put(pair.getKey(), valueFactory.createIRI(pair.getValue()));
    }
    return prefixesMap;
  }

  private Pair<String, String> getPair(String statement) {
    String[] allComponents = statement.split("\\s+");
    if (allComponents.length == 3) {
      final String key = allComponents[1].substring(0, allComponents[1].length() - 1);
      final String value = allComponents[2].substring(1, allComponents[2].length() - 1);
      return new Pair<>(key, value);
    } else if (allComponents.length == 2) {
      String[] keyValueElements = allComponents[1].split(":");
      if (keyValueElements.length == 2) {
        return new Pair<>(keyValueElements[0], keyValueElements[1]);
      }
    }
    throw new ConfigurationException(
        String.format("Could not get key|value of statement {%s}", statement));
  }

  private Optional<String> getObjectString(Model model, Resource subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }

}
