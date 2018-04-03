package org.dotwebstack.framework.frontend.openapi;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public final class Rdf4jUtils {

  private Rdf4jUtils() {}

  /**
   * @return An in-memory {@code Repository} for the supplied {@code Model}.
   */
  public static Repository asRepository(@NonNull Model model) {
    Repository repository = new SailRepository(new MemoryStore());

    repository.initialize();

    try (RepositoryConnection connection = repository.getConnection()) {
      connection.add(model);
    }

    return repository;
  }

  /**
   * @return The resources resulting from evaluating the supplied SPARQL SELECT query on the
   *         specified repository. The query must have defined exactly one binding and must return
   *         resources (IRIs and blank nodes) only.
   * @throws QueryEvaluationException If the query has &gt; 1 binding defined. Or if the result
   *         contains a non resource.
   */
  public static Set<Resource> evaluateSingleBindingSelectQuery(@NonNull Repository repository,
      @NonNull String query) {
    try (RepositoryConnection connection = repository.getConnection()) {
      TupleQuery tupleQuery = connection.prepareTupleQuery(query);

      try (TupleQueryResult result = tupleQuery.evaluate()) {
        List<String> bindingNames = result.getBindingNames();

        checkNoBindingNamesEqualTo1(query, bindingNames);

        String bindingName = bindingNames.get(0);
        ImmutableSet.Builder<Resource> builder = ImmutableSet.builder();

        while (result.hasNext()) {
          BindingSet bindingSet = result.next();
          Value value = bindingSet.getValue(bindingName);

          checkValueInstanceOfResource(query, value);

          builder.add((Resource) value);
        }

        return builder.build();
      }
    }
  }

  private static void checkNoBindingNamesEqualTo1(String query, List<String> bindingNames) {
    if (bindingNames.size() != 1) {
      throw new QueryEvaluationException(
          String.format("Query must define exactly 1 binding: '%s'", query));
    }
  }

  private static void checkValueInstanceOfResource(String query, Value value) {
    if (!(value instanceof Resource)) {
      throw new QueryEvaluationException(
          String.format("Query must return RDF resources (IRIs and blank nodes) only. "
              + "Query string: '%s'%nValue returned: '%s'", query, value));
    }
  }

}
