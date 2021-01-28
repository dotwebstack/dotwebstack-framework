package org.dotwebstack.framework.core.datafetchers;

import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.filters.CompositeFilter;
import org.dotwebstack.framework.core.datafetchers.filters.FieldFilter;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
import org.springframework.stereotype.Component;
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
    ExecutionStepInfo executionStepInfo = environment.getExecutionStepInfo();
    Map<String, Object> source = environment.getSource();
    TypeConfiguration<?> typeConfiguration = getTypeConfiguration(environment.getFieldType()).orElseThrow();

    if (source != null) {
      String resultKey = executionStepInfo.getResultKey();

      // Check if data is already present (eager-loaded)
      if (source.containsKey(resultKey)) {
        if (source.get(resultKey) instanceof Map && ((Map<?, ?>) source.get(resultKey)).size() > 0) {
          return source.get(resultKey);
        }
        return null;
      }

      // Create separate dataloader for every unique path, since evert path can have different arguments
      // or selection
      String dataLoaderKey = String.join("/", executionStepInfo.getPath()
          .getKeysOnly());

      // Retrieve dataloader instance for key, or create new instance when it does not exist yet
      DataLoader<Object, List<DataFetcherResult<Map<String, Object>>>> dataLoader = environment.getDataLoaderRegistry()
          .computeIfAbsent(dataLoaderKey, key -> this.createDataLoader(environment, typeConfiguration));

      String fieldName = environment.getFieldDefinition()
          .getName();

      if (!source.containsKey(fieldName)) {
        LocalDataFetcherContext localDataFetcherContext = environment.getLocalContext();
        Filter filter;

        if (localDataFetcherContext.getFieldFilters()
            .containsKey(fieldName)) {
          filter = localDataFetcherContext.getFieldFilters()
              .get(fieldName);
        } else {
          // TODO: Filter moet uit de parent komen of dynamisch bepaald moeten kunnen worden
          filter = FieldFilter.builder()
              .field("beers_identifier")
              .value(source.get("identifier"))
              .build();
        }

        return dataLoader.load(filter);
      }
    }

    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(environment.getFieldType());
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    Optional<Filter> key = getFilter(environment);

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .queryName(environment.getFieldDefinition()
            .getName())
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .executionStepInfo(environment.getExecutionStepInfo())
        .selectedFields(environment.getSelectionSet()
            .getImmediateFields())
        .build();

    // R: loadSingle (cardinality is one-to-one or many-to-one)
    // R2: Is key passed als field argument? (TBD: only supported for query field? source always null?)
    // => get key from field argument
    if (!GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldType()))) {
      return backendDataLoader.loadSingle(key.orElse(null), loadEnvironment)
          .toFuture();
    }

    return backendDataLoader.loadMany(key.orElse(null), loadEnvironment)
        .map(row -> getDataFetcherResult(row, typeConfiguration, getListSelectedFields(environment)))
        .collectList()
        .toFuture();
  }

  private List<SelectedField> getListSelectedFields(DataFetchingEnvironment environment) {
    return environment.getSelectionSet()
        .getImmediateFields()
        .stream()
        .filter(selectedField -> GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(selectedField.getFieldDefinition()
            .getType())))
        .collect(Collectors.toList());
  }


  private DataFetcherResult<Object> getDataFetcherResult(Map<String, Object> data,
      TypeConfiguration<?> typeConfiguration, List<SelectedField> selectedFields) {
    return DataFetcherResult.newResult()
        .data(data)
        .localContext(LocalDataFetcherContext.builder()
            .fieldFilters(createFilters(data, typeConfiguration, selectedFields))
            .build())
        .build();
  }

  private Map<String, Filter> createFilters(Map<String, Object> data, TypeConfiguration<?> typeConfiguration,
      List<SelectedField> selectedFields) {
    Map<String, Filter> filters = new HashMap<>();

    for (SelectedField selectedField : selectedFields) {
      createFilter(typeConfiguration, data, selectedField).ifPresent(f -> filters.put(selectedField.getName(), f));
    }

    return filters;
  }

  public Optional<Filter> createFilter(TypeConfiguration<?> typeConfiguration, Map<String, Object> data,
      SelectedField selectedField) {
    AbstractFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    // one-to-many (mappedBy)
    if (fieldConfiguration != null) {
      if (fieldConfiguration.getMappedBy() != null) {
        String typeName = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
            .getType())
            .getName();
        TypeConfiguration<?> type = dotWebStackConfiguration.getTypeMapping()
            .get(typeName);

        String mappedBy = fieldConfiguration.getMappedBy();

        return Optional.of(type.getFields()
            .get(mappedBy)
            .createMappedByFilter(data));
      }
    }

    return Optional.empty();
  }

  private Optional<TypeConfiguration<?>> getTypeConfiguration(GraphQLOutputType outputType) {
    GraphQLType nullableType = GraphQLTypeUtil.unwrapNonNull(outputType);
    GraphQLUnmodifiedType rawType = GraphQLTypeUtil.unwrapAll(nullableType);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw new IllegalArgumentException("Output is not an object type.");
    }

    return Optional.ofNullable(dotWebStackConfiguration.getTypeMapping()
        .get(rawType.getName()));
  }

  private Optional<Filter> getFilter(DataFetchingEnvironment environment) {

    if (environment.getLocalContext() != null) {
      String fieldName = environment.getFieldDefinition()
          .getName();

      LocalDataFetcherContext context = environment.getLocalContext();

      if (context.getFieldFilters()
          .containsKey(fieldName)) {
        return Optional.of(context.getFieldFilters()
            .get(fieldName));
      }
    }

    List<Filter> filters = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> argument.getDirectives("key")
            .size() > 0)
        .map(argument -> getFieldFilter(environment, argument))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (filters.size() > 1) {
      return Optional.of(CompositeFilter.builder()
          .filters(filters)
          .build());
    }

    if (filters.size() == 1) {
      return Optional.of(filters.get(0));
    }

    return Optional.empty();
  }

  public FieldFilter getFieldFilter(DataFetchingEnvironment environment, GraphQLArgument argument) {
    Object value = environment.getArguments()
        .get(argument.getName());

    if (value == null) {
      return null;
    }

    GraphQLDirective directive = argument.getDirective("key");

    String fieldName = argument.getName();
    if (directive.getArgument("field")
        .getValue() != null) {
      fieldName = directive.getArgument("field")
          .getValue()
          .toString();
    }

    return FieldFilter.builder()
        .field(fieldName)
        .value(value)
        .build();
  }

  private DataLoader<Filter, ?> createDataLoader(DataFetchingEnvironment environment,
      TypeConfiguration<?> typeConfiguration) {
    GraphQLOutputType unwrappedType = environment.getExecutionStepInfo()
        .getUnwrappedNonNullType();
    GraphQLObjectType objectType = (GraphQLObjectType) GraphQLTypeUtil.unwrapAll(unwrappedType);
    BackendDataLoader backendDataLoader = getBackendDataLoader(typeConfiguration).orElseThrow();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .objectType(objectType)
        .executionStepInfo(environment.getExecutionStepInfo())
        .selectedFields(environment.getSelectionSet()
            .getImmediateFields())
        .build();

    // .flatMap(group -> group.collectList().map(list -> new Map.Entry(group.key(), list))

    if (GraphQLTypeUtil.isList(unwrappedType)) {
      return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadMany(keys, loadEnvironment)
          .flatMap(group -> group.map(data -> DataFetcherResult.newResult()
              .data(data)
              .build())
              .collectList()
              .map(list -> createEntry(group.key(), list)))
          .collectMap(Map.Entry::getKey, Map.Entry::getValue)
          .toFuture());
    }

    return DataLoader.newMappedDataLoader(keys -> backendDataLoader.batchLoadSingle(keys, loadEnvironment)
        .collectMap(Tuple2::getT1, tuple -> DataFetcherResult.newResult()
            .data(tuple.getT2())
            .build())
        .toFuture());
  }

  private Map.Entry<Filter, List<DataFetcherResult<Object>>> createEntry(Filter filter,
      List<DataFetcherResult<Object>> list) {
    return Map.entry(filter, list);
  }

  private Optional<BackendDataLoader> getBackendDataLoader(TypeConfiguration<?> typeConfiguration) {
    return backendDataLoaders.stream()
        .filter(loader -> loader.supports(typeConfiguration))
        .findFirst();
  }
}
