package org.dotwebstack.framework.backend.json.query;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

@Getter
public final class JsonSolution {

  private final JsonNode jsonNode;

  public JsonSolution(JsonNode jsonNode) {
    this.jsonNode = jsonNode;
  }
}
