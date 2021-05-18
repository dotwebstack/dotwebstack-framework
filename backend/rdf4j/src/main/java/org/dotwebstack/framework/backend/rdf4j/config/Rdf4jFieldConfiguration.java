package org.dotwebstack.framework.backend.rdf4j.config;

import java.util.List;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;

@Data
@EqualsAndHashCode(callSuper = true)
public class Rdf4jFieldConfiguration extends AbstractFieldConfiguration {

  @Valid
  private List<JoinProperty> joinProperties;

  @Override
  public boolean isScalarField() {
    return false;
  }

  @Override
  public boolean isObjectField() {
    return false;
  }

  @Override
  public boolean isNestedObjectField() {
    return false;
  }
}
