package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.eclipse.rdf4j.query.TupleQueryResult;

public final class TupleEntity extends AbstractEntity {

  private final TupleQueryResult queryResult;

  TupleEntity(@NonNull ApiResponse response, @NonNull TupleQueryResult queryResult,
      @NonNull RequestContext requestContext) {
    super(response, requestContext);
    this.queryResult = queryResult;
  }

  public TupleQueryResult getResult() {
    return queryResult;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ApiResponse response;

    private RequestContext requestContext;

    private TupleQueryResult tupleQueryResult;

    public Builder withResult(@NonNull TupleQueryResult queryResult) {
      this.tupleQueryResult = queryResult;
      return this;
    }

    public Builder withResponse(@NonNull ApiResponse response) {
      this.response = response;
      return this;
    }

    public Builder withRequestContext(@NonNull RequestContext requestContext) {
      this.requestContext = requestContext;
      return this;
    }

    public TupleEntity build() {
      return new TupleEntity(response, tupleQueryResult, requestContext);
    }

  }

}
