package org.dotwebstack.framework.query.transformator;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.query.visitor.FederatedQueryVisitor;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryTransformator {

  private static final Logger LOG = LoggerFactory.getLogger(QueryTransformator.class);

  private static final Pattern regexPrefix = Pattern.compile(
      "(prefix)\\b\\s*[a-zA-Z0-9]\\w*\\s*\\:\\s*\\<.*\\>", Pattern.CASE_INSENSITIVE);

  private static final Pattern regexService = Pattern.compile(
      "(service)\\s*[a-zA-Z0-9]*\\w*\\s*\\:\\s*[a-zA-Z0-9]*\\w+", Pattern.CASE_INSENSITIVE);

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private BackendResourceProvider backendResourceProvider;

  @Autowired
  public QueryTransformator(@NonNull BackendResourceProvider backendResourceProvider) {
    this.backendResourceProvider = backendResourceProvider;
  }

  public String transformQuery(String query, IRI supportedStep) {
    if (query != null) {
      SPARQLParserFactory sparqlParserFactory = new SPARQLParserFactory();
      QueryParser queryParser = sparqlParserFactory.getParser();
      FederatedQueryVisitor visitor = new FederatedQueryVisitor(backendResourceProvider);
      if (supportedStep.equals(ELMO.UPDATE_STEP)) {
        ParsedUpdate parsedUpdate =
            queryParser.parseUpdate(query, ELMO.CONFIG_GRAPHNAME.toString());

        parsedUpdate.getUpdateExprs().forEach(updateExprs -> updateExprs.visit(visitor));
      } else {
        ParsedQuery parsedQuery = queryParser.parseQuery(query, ELMO.CONFIG_GRAPHNAME.toString());
        parsedQuery.getTupleExpr().visit(visitor);
      }
      if (!visitor.getReplaceableBackends().isEmpty()) {
        return setBackendUri(query, visitor.getReplaceableBackends());
      }
    }
    return query;
  }

  public Optional<String> transformQuery(Optional<String> query) {
    if (query.isPresent()) {
      SPARQLParserFactory sparqlParserFactory = new SPARQLParserFactory();
      QueryParser queryParser = sparqlParserFactory.getParser();

      ParsedQuery parsedQuery =
          queryParser.parseQuery(query.get(), ELMO.CONFIG_GRAPHNAME.toString());
      FederatedQueryVisitor visitor = new FederatedQueryVisitor(backendResourceProvider);
      parsedQuery.getTupleExpr().visit(visitor);
      if (!visitor.getReplaceableBackends().isEmpty()) {
        return Optional.of(setBackendUri(query.get(), visitor.getReplaceableBackends()));
      }
    }
    return query;
  }

  private String setBackendUri(String query, Map<Resource, Backend> backends) {
    String transformedQuery = "";
    Matcher serviceMatcher = regexService.matcher(query);
    Map<String, IRI> prefixesMap = getPrefixesMap(query);
    while (serviceMatcher.find()) {
      SimpleEntry<String, String> servicePair = getPair(serviceMatcher.toMatchResult().group());
      StringBuilder backendKey = new StringBuilder();
      backendKey.append(prefixesMap.get(servicePair.getKey()));
      backendKey.append(servicePair.getValue());
      String replaceString = "SERVICE <"
          + backends.get(valueFactory.createIRI(backendKey.toString())).getEndpoint().stringValue()
          + ">";
      transformedQuery = query.replace(serviceMatcher.toMatchResult().group(), replaceString);
      query = transformedQuery;
    }
    LOG.debug("Transformed query result {}", transformedQuery);
    return transformedQuery;
  }

  private Map<String, IRI> getPrefixesMap(String query) {
    Map<String, IRI> prefixesMap = new HashMap<>();
    Matcher matcher = regexPrefix.matcher(query);
    while (matcher.find()) {
      SimpleEntry<String, String> pair = getPair(matcher.toMatchResult().group());
      prefixesMap.put(pair.getKey(), valueFactory.createIRI(pair.getValue()));
    }
    return prefixesMap;
  }

  private SimpleEntry<String, String> getPair(String statement) {
    String[] allComponents = statement.split("\\s+");
    if (allComponents.length == 3) {
      final String key = allComponents[1].substring(0, allComponents[1].length() - 1);
      final String value = allComponents[2].substring(1, allComponents[2].length() - 1);
      return new SimpleEntry<>(key, value);
    } else if (allComponents.length == 2) {
      String[] keyValueElements = allComponents[1].split(":");
      if (keyValueElements.length == 2) {
        return new SimpleEntry<>(keyValueElements[0], keyValueElements[1]);
      }
    }
    throw new ConfigurationException(
        String.format("Could not get key|value of statement {%s}", statement));
  }

}
