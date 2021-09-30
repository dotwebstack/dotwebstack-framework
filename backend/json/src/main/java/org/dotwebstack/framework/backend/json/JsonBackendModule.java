package org.dotwebstack.framework.backend.json;

import org.dotwebstack.framework.backend.json.model.JsonObjectType;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.springframework.stereotype.Component;

@Component
class JsonBackendModule implements BackendModule<JsonObjectType> {

  @Override
  public Class<JsonObjectType> getObjectTypeClass() {
    return JsonObjectType.class;
  }
}
