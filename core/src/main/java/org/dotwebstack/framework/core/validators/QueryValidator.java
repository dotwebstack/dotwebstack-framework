package org.dotwebstack.framework.core.validators;

import graphql.schema.DataFetchingEnvironment;
import lombok.NonNull;

public interface QueryValidator {
  void validate(@NonNull DataFetchingEnvironment dataFetchingEnvironment);
}
