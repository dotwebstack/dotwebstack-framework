package org.dotwebstack.framework.frontend.openapi.entity.schema;

import org.eclipse.rdf4j.model.Value;

/**
 * This context is a mutable holder for all data/properties/values needed in API structures. Data
 * can be propagated into nested or deeper structures.
 */
// XXX (PvH) Waarom een interface (als we maar één implementatie hebben)?
public interface SchemaMapperContext {

  // XXX (PvH) Waarom een Boolean? (en geen boolean)
  Boolean isExcludedWhenEmpty();

  // XXX (PvH) Idem
  Boolean isExcludedWhenNull();

  Value getValue();

  // XXX (PvH) Ik zou de context immutable maken. De value kunnen zetten is gevaarlijk (zoals we
  // hebben gezien) :-)
  // Mogelijk kunnen we een factory method maken?
  void setValue(Value value);

  // XXX (PvH) Idem
  void setExcludedWhenNull(boolean includedWhenNull);

  // XXX (PvH) Idem
  void setExcludedWhenEmpty(boolean includedWhenEmpty);

}
