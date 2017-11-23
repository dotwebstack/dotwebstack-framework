package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.eclipse.rdf4j.query.TupleQueryResult;

public final class TupleEntity extends AbstractEntity<TupleQueryResult> {

  TupleEntity(Map<MediaType, Property> schemaProperty, TupleQueryResult queryResult) {
    super(schemaProperty, queryResult);
  }

  @Override
  public TupleQueryResult getResult() {
    return getQueryResultDb();
  }

  @Override
  public Map<MediaType, Property> getSchemaMap() {
    return super.getSchemaMap();
  }

  @Override
  public EntityContext getEntityContext() {
    throw new UnsupportedOperationException("No entity context available");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Map<MediaType, Property> schemaMap;

    private TupleQueryResult tupleQueryResult;

    public TupleEntity build() {
      return new TupleEntity(schemaMap, tupleQueryResult);
    }

    public Builder withQueryResult(TupleQueryResult queryResult) {
      this.tupleQueryResult = queryResult;
      return this;
    }

    public Builder withSchemaMap(Map<MediaType, Property> schemaMap) {
      this.schemaMap = schemaMap;
      return this;
    }


  }
}
