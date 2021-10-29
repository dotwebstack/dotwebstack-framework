package org.dotwebstack.framework.backend.rdf4j.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.model.AbstractObjectField;

@Data
@EqualsAndHashCode(callSuper = true)
public class Rdf4jObjectField extends AbstractObjectField {

  private boolean resource = false;
}
