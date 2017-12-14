package org.dotwebstack.framework.frontend.openapi.entity.schema;

import lombok.Builder;
import org.eclipse.rdf4j.model.Value;

/**
 * This context is a mutable holder for all data/properties/values needed in API structures. Data
 * can be propagated into nested or deeper structures.
 */
@Builder(toBuilder = true)
@lombok.Value
public class ValueContext {

  private final boolean isExcludedWhenEmptyOrNull;

  private final Value value;

}
