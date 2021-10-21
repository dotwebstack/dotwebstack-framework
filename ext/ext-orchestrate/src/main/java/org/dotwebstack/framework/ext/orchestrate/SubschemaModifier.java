package org.dotwebstack.framework.ext.orchestrate;

import org.dotwebstack.graphql.orchestrate.schema.Subschema;

@FunctionalInterface
public interface SubschemaModifier {

  Subschema modify(String key, Subschema subschema);
}
