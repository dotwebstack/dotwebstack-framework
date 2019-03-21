package org.dotwebstack.framework.core.graphql;

import graphql.schema.idl.SchemaDirectiveWiring;

public interface NamedSchemaDirectiveWiring extends SchemaDirectiveWiring {

  String getName();

}
