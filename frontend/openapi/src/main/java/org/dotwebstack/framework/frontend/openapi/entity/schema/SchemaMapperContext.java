package org.dotwebstack.framework.frontend.openapi.entity.schema;

import org.eclipse.rdf4j.model.Value;

/**
 * This context is a mutable holder for all data/properties/values needed in API structures. Data
 * can be propagated into nested or deeper structures.
 */
public interface SchemaMapperContext {

  Boolean isExcludedWhenEmpty();

  Boolean isExcludedWhenNull();

  Value getValue();

  void setValue(Value value);

  void setExcludedWhenNull(boolean includedWhenNull);

  void setExcludedWhenEmpty(boolean includedWhenEmpty);

}
