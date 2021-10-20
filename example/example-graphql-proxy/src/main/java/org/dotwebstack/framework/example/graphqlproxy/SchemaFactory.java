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

// @Component
public class SchemaFactory {

  private final Resource schemaFile;

  private final GraphQLSchema schema;

  public SchemaFactory(@Value("classpath:config/schema.graphql") Resource schemaFile, GraphQLSchema schema) {
    this.schemaFile = schemaFile;
    this.schema = schema;
  }

  public GraphQLSchema create() throws IOException {
    var typeDefinitionRegistry = new SchemaParser().parse(schemaFile.getInputStream());

    var subschema = Subschema.builder()
        .schema(schema)
        .build();

    var codeReqistry = GraphQLCodeRegistry.newCodeRegistry()
        .dataFetcher(FieldCoordinates.coordinates("Query", "zoek"), new ZoekResultaatDataFetcher(subschema))
        .build();

    var runtimeWiring = RuntimeWiring.newRuntimeWiring()
        .scalar(CoreScalars.OBJECT)
        .codeRegistry(codeReqistry)
        .build();

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
  }
}
