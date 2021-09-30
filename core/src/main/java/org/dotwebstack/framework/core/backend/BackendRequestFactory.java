package org.dotwebstack.framework.core.backend;

import graphql.schema.DataFetchingEnvironment;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.springframework.stereotype.Component;

@Component
public class BackendRequestFactory {

  private final Schema schema;

  public BackendRequestFactory(Schema schema) {
    this.schema = schema;
  }

  public CollectionRequest createCollectionRequest(DataFetchingEnvironment environment) {
    return null;
  }

  public ObjectRequest createObjectRequest(DataFetchingEnvironment environment) {
    return null;
  }
}
