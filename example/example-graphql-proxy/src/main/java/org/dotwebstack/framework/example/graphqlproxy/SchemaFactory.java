package org.dotwebstack.framework.example.graphqlproxy;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import java.io.IOException;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.dotwebstack.graphql.orchestrate.schema.Subschema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class SchemaFactory {

  private final Resource schemaFile;

  private final Subschema subschema;

  public SchemaFactory(@Value("classpath:config/schema.graphql") Resource schemaFile, Subschema subschema) {
    this.schemaFile = schemaFile;
    this.subschema = subschema;
  }

  public GraphQLSchema create() throws IOException {
    var typeDefinitionRegistry = new SchemaParser()
        .parse(schemaFile.getInputStream());

    var codeReqistry = GraphQLCodeRegistry.newCodeRegistry()
        .dataFetcher(FieldCoordinates.coordinates("Query", "zoek"),
            new ZoekResultaatDataFetcher(subschema))
        .build();

    var runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .scalar(CoreScalars.OBJECT)
        .codeRegistry(codeReqistry)
        .build();

    return new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
  }
}
