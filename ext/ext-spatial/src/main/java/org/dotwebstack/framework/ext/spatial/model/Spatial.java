package org.dotwebstack.framework.ext.spatial.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
