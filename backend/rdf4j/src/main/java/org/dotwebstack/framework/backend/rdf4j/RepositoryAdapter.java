package org.dotwebstack.framework.backend.rdf4j;

import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlResult;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.TupleQuery;

public interface RepositoryAdapter {
  TupleQuery prepareTupleQuery(String repositoryId, DataFetchingEnvironment environment, String query);

  GraphQuery prepareGraphQuery(String repositoryId, DataFetchingEnvironment environment, String query,
      List<String> subjectIris);

  BooleanQuery prepareBooleanQuery(String repositoryId, DataFetchingEnvironment environment, String query);

  SparqlResult executeSparqlQuery(String repositoryId, DataFetchingEnvironment environment, String query,
                                  List<String> subjects, Map<String, Value> bindingValues);

  boolean supports(String repositoryId);
}
