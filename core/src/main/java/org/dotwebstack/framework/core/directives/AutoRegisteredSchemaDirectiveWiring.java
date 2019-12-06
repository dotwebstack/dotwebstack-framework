package org.dotwebstack.framework.core.directives;

import graphql.schema.idl.SchemaDirectiveWiring;

public interface AutoRegisteredSchemaDirectiveWiring extends SchemaDirectiveWiring {
  String getDirectiveName();
}
