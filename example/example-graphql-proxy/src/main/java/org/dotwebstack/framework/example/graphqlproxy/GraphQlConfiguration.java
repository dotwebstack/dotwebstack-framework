package org.dotwebstack.framework.example.graphqlproxy;

import java.util.List;
import org.dotwebstack.graphql.orchestrate.transform.HoistField;
import org.dotwebstack.graphql.orchestrate.transform.RenameObjectFields;
import org.dotwebstack.graphql.orchestrate.transform.Transform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQlConfiguration {

  // @Bean
  // @Primary
  // public GraphQL graphQL(SchemaFactory schemaFactory) throws IOException {
  // var schema = schemaFactory.create();
  //
  // return GraphQL.newGraphQL(schema)
  // .build();
  // }

  @Bean
  public Transform transform() {
    return createNameTransform().pipe(createHoistTransform());
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
