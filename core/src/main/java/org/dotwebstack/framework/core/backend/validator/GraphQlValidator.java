package org.dotwebstack.framework.core.backend.validator;

import graphql.schema.DataFetchingEnvironment;

public interface GraphQlValidator {

  void validate(DataFetchingEnvironment environment);
}
