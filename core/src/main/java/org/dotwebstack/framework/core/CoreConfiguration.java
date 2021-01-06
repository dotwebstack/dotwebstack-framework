package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import graphql.GraphQL;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.CombinedWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.dotwebstack.framework.core.jexl.JexlFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

@Slf4j
@Configuration
public class CoreConfiguration {

  private static final String CONFIG_FILE = "dotwebstack.yaml";

  private static final String SCHEMA_FILE = "schema.graphqls";

  private final List<WiringFactory> wiringFactories;

  public CoreConfiguration(List<WiringFactory> wiringFactories) {
    this.wiringFactories = wiringFactories;
  }

  @Bean
  public DotWebStackConfiguration dotWebStackConfiguration() {
    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(TypeConfiguration.class));
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    scanner.findCandidateComponents("org.dotwebstack.framework.backend")
        .stream()
        .map(beanDefinition -> ClassUtils.resolveClassName(
            Objects.requireNonNull(beanDefinition.getBeanClassName()),
            ClassLoader.getSystemClassLoader()))
        .forEach(objectMapper::registerSubtypes);

    return ResourceLoaderUtils.getResource(CONFIG_FILE)
        .map(resource -> {
          try {
            return objectMapper.readValue(resource.getFile(), DotWebStackConfiguration.class);
          } catch (IOException e) {
            throw illegalStateException("Error while reading config file: {}", CONFIG_FILE);
          }
        })
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", CONFIG_FILE));
  }

  @Bean
  public GraphQLSchema graphqlSchema(@NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Collection<GraphqlConfigurer> graphqlConfigurers, @NonNull List<SchemaValidator> schemaValidators) {
    RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring()
        .wiringFactory(new CombinedWiringFactory(wiringFactories));

    graphqlConfigurers.forEach(graphqlConfigurer -> graphqlConfigurer.configureRuntimeWiring(runtimeWiringBuilder));

    graphqlConfigurers
        .forEach(graphqlConfigurer -> graphqlConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistry));

    schemaValidators.forEach(SchemaValidator::validate);

    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  @Profile("!test")
  @Bean
  public TypeDefinitionRegistry typeDefinitionRegistry() throws IOException {
    Optional<Resource> schemaLocationResource = ResourceLoaderUtils.getResource(SCHEMA_FILE);
    if (schemaLocationResource.isEmpty() || !schemaLocationResource.get()
        .exists()) {
      throw invalidConfigurationException("Graphql schema not found on location: {}", SCHEMA_FILE);
    }
    Reader reader = new InputStreamReader(schemaLocationResource.get()
        .getInputStream());

    return new SchemaParser().parse(reader);
  }

  @Bean
  public GraphQL graphql(@NonNull GraphQLSchema graphqlSchema) {
    return GraphQL.newGraphQL(graphqlSchema)
        .instrumentation(new TracingInstrumentation(
            TracingInstrumentation.Options.newOptions().includeTrivialDataFetchers(false)))
        .build();
  }

  @Bean
  public JexlEngine jexlBuilder(List<JexlFunction> jexlFunctions) {
    Map<String, Object> namespaces = jexlFunctions.stream()
        .collect(Collectors.toMap(JexlFunction::getNamespace, function -> function));
    LOG.debug("Loading JEXL functions [{}]", namespaces);
    return new JexlBuilder().silent(false)
        .namespaces(namespaces)
        .strict(true)
        .create();
  }
}
