package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.Response;
import lombok.NonNull;
import org.eclipse.rdf4j.query.TupleQueryResult;

public final class TupleEntity extends AbstractEntity {

  private final TupleQueryResult queryResult;

  TupleEntity(@NonNull Response response, @NonNull TupleQueryResult queryResult) {
    super(response);
    this.queryResult = queryResult;
  }

  public TupleQueryResult getResult() {
    return queryResult;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Response response;

    private TupleQueryResult tupleQueryResult;

    public Builder withQueryResult(@NonNull TupleQueryResult queryResult) {
      this.tupleQueryResult = queryResult;
      return this;
    }

    public Builder withResponse(@NonNull Response response) {
      this.response = response;
      return this;
    }

    public TupleEntity build() {
      return new TupleEntity(response, tupleQueryResult);
    }

  }

}
