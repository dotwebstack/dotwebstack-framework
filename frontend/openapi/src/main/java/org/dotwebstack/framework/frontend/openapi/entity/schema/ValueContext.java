package org.dotwebstack.framework.frontend.openapi.entity.schema;

import lombok.Builder;
import org.eclipse.rdf4j.model.Value;

/**
 * This context is a mutable holder for all data/properties/values needed in API structures. Data
 * can be propagated into nested or deeper structures.
 */
@Builder(toBuilder = true)
public class ValueContext {

  // XXX (PvH) Melding Eclipse: @Builder will ignore the initializing expression entirely. If you
  // want the initializing expression to serve as default, add @Builder.Default. If it is not
  // supposed to be settable during building, make the field final.
  private boolean isExcludedWhenEmpty = false;

  // XXX (PvH) Idem
  private boolean isExcludedWhenNull = false;

  private Value value;

  public boolean isExcludedWhenEmpty() {
    return isExcludedWhenEmpty;
  }

  public boolean isExcludedWhenNull() {
    return isExcludedWhenNull;
  }

  public Value getValue() {
    return value;
  }

}
