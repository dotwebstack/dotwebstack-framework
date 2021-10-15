package org.dotwebstack.framework.example.graphqlproxy;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.io.IOException;
import java.util.List;
import org.dotwebstack.graphql.orchestrate.schema.RemoteExecutor;
import org.dotwebstack.graphql.orchestrate.schema.Subschema;
import org.dotwebstack.graphql.orchestrate.transform.HoistField;
import org.dotwebstack.graphql.orchestrate.transform.RenameObjectFields;
import org.dotwebstack.graphql.orchestrate.transform.Transform;
import org.dotwebstack.graphql.orchestrate.wrap.SchemaWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLConfiguration {

  // @Bean
  public GraphQL graphQL(SchemaFactory schemaFactory) throws IOException {
    var schema = schemaFactory.create();

    return GraphQL.newGraphQL(schema)
        .build();
  }

  @Bean
  public GraphQL graphQL(GraphQLSchema schema, RemoteExecutor executor) {
    var subschema = Subschema.builder()
        .schema(schema)
        .executor(executor)
        .transform(createNameTransform()
            .pipe(createHoistTransform()))
        .build();

    var wrappedSchema = SchemaWrapper.wrap(subschema);

    return GraphQL.newGraphQL(wrappedSchema)
        .build();
  }

  private Transform createNameTransform() {
    return new RenameObjectFields((typeName, fieldName, fieldDefinition) -> {
      // Only rename fields for type Woonplaats
      if (!typeName.equals("Woonplaats")) {
        return fieldName;
      }

      switch (fieldName) {
        case "identificatie":
          return "uri";
        case "naam":
          return "plaatsnaam";
        default:
          return fieldName;
      }
    });
  }

  private Transform createHoistTransform() {
    return new HoistField("Woonplaats", "geoWKT", List.of("geometrie", "asWKT"));
  }
}
