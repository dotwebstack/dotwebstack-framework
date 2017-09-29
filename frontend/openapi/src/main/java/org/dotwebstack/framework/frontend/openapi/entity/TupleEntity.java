package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.eclipse.rdf4j.query.TupleQueryResult;

public final class TupleEntity extends AbstractEntity<TupleQueryResult> {

  private TupleQueryResult result;

  public TupleEntity(@NonNull Map<MediaType, Property> schemaMap,
      @NonNull TupleQueryResult result) {
    super(schemaMap);
    this.result = result;
  }

  public TupleQueryResult getResult() {
    return result;
  }

}
