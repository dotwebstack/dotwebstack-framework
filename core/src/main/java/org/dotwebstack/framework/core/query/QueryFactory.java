package org.dotwebstack.framework.core.query;

import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.springframework.stereotype.Component;

@Component
public class QueryFactory {
  public ObjectQuery createObjectQuery(){
    return new ObjectQuery();
  }
}
