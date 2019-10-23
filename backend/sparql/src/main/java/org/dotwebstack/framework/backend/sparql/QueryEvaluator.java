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
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
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

  public void addToGraph(@NonNull RepositoryConnection repositoryConnection,
      @NonNull Model transactionModel,
      @NonNull IRI targetGraph) {
    try {
      if (transactionModel.contexts().isEmpty()) {
        repositoryConnection.add(transactionModel, targetGraph);
        LOG.debug("Insert data into systemGraph {}", targetGraph);
      } else {
        transactionModel.contexts().forEach(graphName -> {
          if (graphName != null) {
            repositoryConnection.add(
                transactionModel.stream().filter(
                    statement -> statement.getContext().equals(graphName)).collect(
                        Collectors.toList()),
                targetGraph);
            LOG.debug("Insert data into namedGraph {}", targetGraph);
          } else {
            repositoryConnection.add(transactionModel, targetGraph);
            LOG.debug("Insert data into default graph {}", targetGraph);
          }
        });
      }
    } catch (RDF4JException e) {
      LOG.debug("Data could not be added to graph: {}", e.getMessage());
      throw new BackendException(
          String.format("Data could not be added to graph: %s", e.getMessage()), e);
    } catch (Exception ex) {
      LOG.debug("Data could not be added into graph: {} \n {}", ex.getMessage(), ex);
      throw new BackendException(
          String.format("Data could not be added to graph: %s", ex.getMessage()), ex);
    }
  }

  public void addToGraphs(@NonNull RepositoryConnection repositoryConnection,
      @NonNull Model transactionModel) {
    try {
      transactionModel.contexts().forEach(graphName -> {
        if (graphName != null) {
          repositoryConnection.add(
              transactionModel.stream().filter(
                  statement -> statement.getContext().equals(graphName)).collect(
                      Collectors.toList()),
              graphName);
          LOG.debug("Insert data into namedGraph {}", graphName);
        }
      });
    } catch (RDF4JException e) {
      LOG.debug("Data could not be added to graph: {}", e.getMessage());
      throw new BackendException(
          String.format("Data could not be added to graph: %s", e.getMessage()), e);
    } catch (Exception ex) {
      LOG.debug("Data could not be added into graph: {} \n {}", ex.getMessage(), ex);
      throw new BackendException(
          String.format("Data could not be added to graph: %s", ex.getMessage()), ex);
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

}
