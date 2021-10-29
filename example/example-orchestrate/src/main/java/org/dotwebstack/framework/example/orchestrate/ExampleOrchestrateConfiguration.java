package org.dotwebstack.framework.example.orchestrate;

import java.util.List;
import org.dotwebstack.framework.ext.orchestrate.SubschemaModifier;
import org.dotwebstack.graphql.orchestrate.transform.HoistField;
import org.dotwebstack.graphql.orchestrate.transform.RenameObjectFields;
import org.dotwebstack.graphql.orchestrate.transform.Transform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExampleOrchestrateConfiguration {

  @Bean
  public SubschemaModifier subschemaModifier() {
    return (key, subschema) -> subschema
        .transform(builder -> builder.transform(createNameTransform().pipe(createHoistTransform())));
  }

  private Transform createNameTransform() {
    return new RenameObjectFields((typeName, fieldName, fieldDefinition) -> {
      // Only rename fields for type Beer
      if (!typeName.equals("Beer")) {
        return fieldName;
      }

      switch (fieldName) {
        case "name":
          return "productLabel";
        case "abv":
          return "abvPercentage";
        default:
          return fieldName;
      }
    });
  }

  private Transform createHoistTransform() {
    return new HoistField("Beer", "breweryName", List.of("brewery", "name"));
  }
}
