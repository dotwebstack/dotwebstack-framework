package org.dotwebstack.framework.backend.rdf4j.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Rdf4jObjectField extends AbstractObjectField {

  private boolean resource = false;
}
