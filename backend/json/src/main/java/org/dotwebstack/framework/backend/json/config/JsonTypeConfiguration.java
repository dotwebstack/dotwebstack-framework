package org.dotwebstack.framework.backend.json.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("json")
public class JsonTypeConfiguration extends AbstractTypeConfiguration<JsonFieldConfiguration> {

  @NotBlank
  private String path;
}
