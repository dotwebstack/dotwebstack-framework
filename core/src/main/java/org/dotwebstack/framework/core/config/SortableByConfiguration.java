package org.dotwebstack.framework.core.config;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.model.SortDirection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortableByConfiguration {

  @NotBlank
  private String field;

  @NonNull
  private SortDirection direction;
}
