package org.dotwebstack.framework.ext.spatial.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import org.dotwebstack.framework.ext.spatial.model.validation.ValidEquivalent;

@Data
public class Spatial {

  @Valid
  @ValidEquivalent
  @NotEmpty
  @JsonProperty("srid")
  private Map<@NotNull Integer, @NotNull SpatialReferenceSystem> referenceSystems;
}
