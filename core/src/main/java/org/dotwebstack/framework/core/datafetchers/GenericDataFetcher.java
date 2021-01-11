package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.keys.FieldKey;
import org.dotwebstack.framework.core.datafetchers.keys.FilterKey;
import org.dotwebstack.framework.core.datafetchers.keys.Key;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@Component
@Slf4j
public final class GenericDataFetcher implements DataFetcher<Object> {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final Collection<BackendDataLoader> backendDataLoaders;

  public GenericDataFetcher(DotWebStackConfiguration dotWebStackConfiguration,
      Collection<BackendDataLoader> backendDataLoaders) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.backendDataLoaders = backendDataLoaders;
  }

  public Object get(DataFetchingEnvironment environment) {
    // Is nested field (source not null)?
    // => Is relationship owned by left-side?
    // => get foreign key from source object (??)
    // => apply key as primary key condition in where clause
    // => Is relationship owned by right-side? (only applicable for one-to-one?)
    // => get primary key from source object (??)
    // => apply key as foreign key condition in where clause
    // => Is relationship owned by junction table?
    // => get primary key from source object (??)
    // => inner join with junction table, qualified by join column
    // => apply key as additional key condition in ON clause
    // => Throw exception when more than one rows is returned
    // Is batching request?
    // => Rewrite key condition from "=" to "IN"
    // Is list?
    // => loadMany (cardinality is one-to-many or many-to-many)
    // => filtering / sorting / paginering is relevant
    // Is nested field (source not null)?
    // => Is relationship owned by right-side?
    // =>
    // => Is relationship owned by junction table?
    // =>
    // Is batching request?
    // => Put source keys in temporary constant table (VALUES)
    // => Wrap query in lateral sub-query (left-joined)
    // TBD: Composite keys
    // TBD: how to deal with rich value-objects (e.g. Geometry)


    ExecutionStepInfo executionStepInfo = environment.getExecutionStepInfo();
    Map<String, Object> source = environment.getSource();

    // TODO: improve type safety & error handling
    GraphQLType outputType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldType());
    GraphQLObjectType rawType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(outputType);
    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeMapping()
        .get(rawType.getName());

    // Loop through keys (max 1 currently)
    // Check presence of argument with corresponding field name
    // If present, apply key condition
    // Validate field is non-list object field

    if (source != null) {
      String dataLoaderKey = String.join("/", executionStepInfo.getPath()
          .getKeysOnly());

      DataLoader<Object, List<Map<String, Object>>> dataLoader = environment.getDataLoaderRegistry()
          .computeIfAbsent(dataLoaderKey, key -> this.createDataLoader(environment, typeConfiguration));

      String fieldName = environment.getFieldDefinition()
          .getName();

      TypeConfiguration<?> parentTypeConfiguration = dotWebStackConfiguration.getTypeMapping()
          .get(GraphQLTypeUtil.unwrapAll(environment.getParentType())
              .getName());

      // Experimental support for right-side relationship (mappedBy)
      if (parentTypeConfiguration.getFields()
          .get(fieldName)
          .getMappedBy() != null) {

        String keyFieldName = parentTypeConfiguration.getKeys()
            .get(0)
            .getField();
        Object key = source.get(keyFieldName);

        String mappedBy = parentTypeConfiguration.getFields()
            .get(fieldName)
            .getMappedBy();

        FilterKey filterKey = FilterKey.builder()
            .path(List.of(mappedBy, keyFieldName))
            .value(key)
            .build();

        return dataLoader.load(filterKey);
      } else if (source.containsKey(fieldName)) {
        return dataLoader.load(source.get(fieldName));
      }
    }

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getFieldType());
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .selectedFields(environment.getSelectionSet()
            .getImmediateFields()
            .stream()
            .map(SelectedField::getName)
            .collect(Collectors.toList()))
        .build();

    Optional<Key> key = getKey(environment);

    // R: loadSingle (cardinality is one-to-one or many-to-one)
    // R2: Is key passed als field argument? (TBD: only supported for query field? source always null?)
    // => get key from field argument
    if (key.isPresent() || !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))) {
      return backendDataLoader.loadSingle(key.orElse(null), loadEnvironment)
          .toFuture();
    }

    return backendDataLoader.loadMany(null, loadEnvironment)
        .collectList()
        .toFuture();

  }

  private Optional<Key> getKey(DataFetchingEnvironment environment) {
    List<FieldKey> fieldKeys = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> argument.getDirectives("key") != null)
        .map(argument -> getFieldKey(environment, argument))
        .collect(Collectors.toList());

    if (fieldKeys.size() == 1) {
      return Optional.of(fieldKeys.get(0));
    }

    return Optional.empty();
  }

  private FieldKey getFieldKey(DataFetchingEnvironment environment, GraphQLArgument argument) {
    // FIXME: magic word
    GraphQLDirective directive = argument.getDirective("key");

    String keyName = argument.getName();
    if (directive.getArgument("field") != null) {
      keyName = directive.getArgument("field")
          .getValue()
          .toString();
    }

    Object keyValue = environment.getArguments()
        .get(keyName);

    return FieldKey.builder()
        .name(keyName)
        .value(keyValue)
        .build();
  }

  private DataLoader<Object, ?> createDataLoader(DataFetchingEnvironment environment,
      TypeConfiguration<?> typeConfiguration) {
    GraphQLOutputType unwrappedType = environment.getExecutionStepInfo()
        .getUnwrappedNonNullType();
    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(unwrappedType);
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .selectedFields(environment.getSelectionSet()
            .getImmediateFields()
            .stream()
            .map(SelectedField::getName)
            .collect(Collectors.toList()))
        .build();

    if (GraphQLTypeUtil.isList(unwrappedType)) {
      return DataLoader.newDataLoader(requests -> backendDataLoader.batchLoadMany(requests, loadEnvironment)
          .flatMapSequential(Flux::collectList)
          .collectList()
          .toFuture());
    }

    return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadSingle(keys, loadEnvironment)
        .collectMap(Tuple2::getT1, Tuple2::getT2)
        .toFuture());
  }

  private Optional<BackendDataLoader> getBackendDataLoader(TypeConfiguration<?> typeConfiguration) {
    return backendDataLoaders.stream()
        .filter(loader -> loader.supports(typeConfiguration))
        .findFirst();
  }
}
