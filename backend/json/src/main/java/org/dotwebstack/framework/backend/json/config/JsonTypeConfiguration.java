package org.dotwebstack.framework.backend.json.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Map;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("json")
public class JsonTypeConfiguration extends AbstractTypeConfiguration<JsonFieldConfiguration> {

  @Valid
  private Map<String, String> queryPaths;

  public String getJsonPathTemplate(String queryName) {
    if (queryPaths.containsKey(queryName)) {
      return queryPaths.get(queryName);
    }

    throw illegalArgumentException("No path configured for query with name '{}'", queryName);
  }
}
