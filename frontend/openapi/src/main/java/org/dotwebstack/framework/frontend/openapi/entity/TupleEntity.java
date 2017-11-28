package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.eclipse.rdf4j.query.TupleQueryResult;

public final class TupleEntity extends AbstractEntity {

  private final TupleQueryResult queryResult;

  TupleEntity(@NonNull Map<MediaType, Property> schemaProperty,
      @NonNull TupleQueryResult queryResult) {
    super(schemaProperty);
    this.queryResult = queryResult;
  }

  public TupleQueryResult getResult() {
    return queryResult;
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return super.getSchemaMap();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Map<MediaType, Property> schemaMap;

    private TupleQueryResult tupleQueryResult;

    public Builder withQueryResult(TupleQueryResult queryResult) {
      this.tupleQueryResult = queryResult;
      return this;
    }

    public Builder withSchemaMap(Map<MediaType, Property> schemaMap) {
      this.schemaMap = schemaMap;
      return this;
    }

    public TupleEntity build() {
      return new TupleEntity(schemaMap, tupleQueryResult);
    }

  }
}
