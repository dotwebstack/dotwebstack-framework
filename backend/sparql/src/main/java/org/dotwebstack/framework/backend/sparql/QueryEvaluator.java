package org.dotwebstack.framework.backend.sparql;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QueryEvaluator {

  private static final Logger LOG = LoggerFactory.getLogger(QueryEvaluator.class);

  public Object evaluate(@NonNull RepositoryConnection repositoryConnection, @NonNull String query,
      @NonNull Map<String, Value> bindings) {
    Query preparedQuery;

    try {
      preparedQuery = repositoryConnection.prepareQuery(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s", query), e);
    }

    bindings.forEach(preparedQuery::setBinding);

    if (preparedQuery instanceof GraphQuery) {
      try {
        return ((GraphQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(String.format("Query could not be evaluated: %s", query), e);
      }
    }

    if (preparedQuery instanceof TupleQuery) {
      try {
        return ((TupleQuery) preparedQuery).evaluate();
      } catch (QueryEvaluationException e) {
        throw new BackendException(String.format("Query could not be evaluated: %s", query), e);
      }
    }

    throw new BackendException(
        String.format("Query type '%s' not supported.", preparedQuery.getClass()));
  }

  public void add(@NonNull RepositoryConnection repositoryConnection, @NonNull Model model,
      @NonNull IRI targetGraph) {
    try {
      if (queryContainsBNode(model)) {
        GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL,
            getInsertQuery(model, targetGraph));
        query.evaluate();
        LOG.debug("Insert data into targetGraph {}", targetGraph);
      } else {
        addMultipleStatements(repositoryConnection, model, targetGraph);
      }
    } catch (RDF4JException e) {
      LOG.debug("Data could not be added into graph: {}", e.getMessage());
      throw new BackendException(
          String.format("Data could not be added into graph: %s", e.getMessage()), e);
    } catch (Exception ex) {
      LOG.debug("Data could not be added into graph: {} \n {}", ex.getMessage(), ex);
    }
  }

  public void update(@NonNull RepositoryConnection repositoryConnection, @NonNull String query,
      @NonNull Map<String, Value> bindings) {
    Update preparedQuery;

    try {
      preparedQuery = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, query);
    } catch (RDF4JException e) {
      throw new BackendException(String.format("Query could not be prepared: %s", query), e);
    }

    bindings.forEach(preparedQuery::setBinding);

    try {
      preparedQuery.execute();
    } catch (QueryEvaluationException e) {
      throw new BackendException(String.format("Query could not be executed: %s", query), e);
    }
  }

  private void addMultipleStatements(@NonNull RepositoryConnection repositoryConnection,
      @NonNull Model model, @NonNull IRI systemGraph) {
    if (model.contexts().isEmpty()) {
      repositoryConnection.add(model, systemGraph);
      LOG.debug("Insert data into systemGraph {}", systemGraph);
    } else {
      model.contexts().forEach(graphName -> {
        if (graphName != null) {
          repositoryConnection.add(
              model.stream().filter(statement -> statement.getContext().equals(graphName)).collect(
                  Collectors.toList()),
              graphName);
          LOG.debug("Insert data into namedGraph {}", graphName);
        } else {
          repositoryConnection.add(model, systemGraph);
          LOG.debug("Insert data into default graph {}", systemGraph);
        }
      });
    }
  }

  private String getInsertQuery(Model model, IRI systemGraph) {
    StringBuilder insertQueryBuilder = new StringBuilder();
    insertQueryBuilder.append("INSERT {\n");
    if (model.contexts().isEmpty()) {
      insertQueryBuilder.append("GRAPH <" + systemGraph.stringValue() + "> {\n");
      model.forEach(statement -> {
        insertQueryBuilder.append(getSubjectString(statement));
        insertQueryBuilder.append(getPredicateString(statement));
        insertQueryBuilder.append(getObjectString(statement));
        insertQueryBuilder.append(".\n");
      });
      insertQueryBuilder.append("}\n};\n");
    } else {
      model.contexts().forEach(graphName -> {
        if (graphName != null) {
          insertQueryBuilder.append(getGraphString(graphName));
        } else {
          insertQueryBuilder.append(getGraphString(systemGraph));
        }
        model.forEach(statement -> {
          insertQueryBuilder.append(getSubjectString(statement));
          insertQueryBuilder.append(getPredicateString(statement));
          insertQueryBuilder.append(getObjectString(statement));
          insertQueryBuilder.append(".\n");
        });
        insertQueryBuilder.append("}\n};\n");
      });
    }

    final String insertQuery = insertQueryBuilder.toString();
    LOG.debug("Transformed INSERT query: \n{}", insertQuery);

    return insertQuery;
  }

  private String getSubjectString(Statement statement) {
    if (statement.getSubject() instanceof BNode) {
      return " " + statement.getSubject();
    }
    return " <" + statement.getSubject() + "> ";
  }

  private String getPredicateString(Statement statement) {
    if (statement.getPredicate() instanceof BNode) {
      return " " + statement.getPredicate();
    }
    return " <" + statement.getPredicate() + "> ";
  }

  private String getObjectString(Statement statement) {
    if (statement.getObject() instanceof BNode) {
      return " " + statement.getObject();
    }
    return " <" + statement.getObject() + "> ";
  }

  private String getGraphString(Resource graphName) {
    return "GRAPH <" + graphName.stringValue() + "> {\n";
  }

  private boolean queryContainsBNode(Model model) {
    return model.subjects().stream().anyMatch(subject -> subject instanceof BNode);
  }

}
