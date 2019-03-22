package org.dotwebstack.framework.core.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.FileNotFoundException;
import java.util.Collection;
import org.dotwebstack.framework.core.graphql.scalars.ScalarProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
public class GraphqlConfiguration {

  private static final String SCHEMA_PATH = "classpath:config/schema.graphqls";

  @Bean
  public GraphQLSchema graphqlSchema(Collection<NamedSchemaDirectiveWiring> directives,
      Collection<ScalarProvider> scalarProviders) throws FileNotFoundException {
    TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser()
        .parse(ResourceUtils.getFile(SCHEMA_PATH));

    RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();

    // Register custom directives
    directives.forEach(directive -> runtimeWiringBuilder.directive(directive.getName(), directive));

    // Register custom scalar types
    scalarProviders.forEach(scalarProvider -> scalarProvider.getScalarTypes()
        .forEach(runtimeWiringBuilder::scalar));

    return new SchemaGenerator()
        .makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  @Bean
  public GraphQL graphql(GraphQLSchema graphqlSchema) {
    return GraphQL
        .newGraphQL(graphqlSchema)
        .build();
  }

}
