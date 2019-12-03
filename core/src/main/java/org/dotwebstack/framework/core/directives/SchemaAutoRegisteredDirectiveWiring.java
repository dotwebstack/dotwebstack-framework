package org.dotwebstack.framework.core.directives;

import graphql.schema.idl.SchemaDirectiveWiring;

public interface SchemaAutoRegisteredDirectiveWiring extends SchemaDirectiveWiring {
  String getDirectiveName();
}
