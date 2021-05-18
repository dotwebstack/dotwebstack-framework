package org.dotwebstack.framework.core.query;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionQuery;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.springframework.stereotype.Component;

@Component
public class QueryFactory {

  public CollectionQuery createCollectionQuery(TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return CollectionQuery.builder()
        .objectQuery(createObjectQuery(typeConfiguration, environment))
        .pagingCriteria(PagingCriteria.builder()
            .page(0)
            .pageSize(10)
            .build())
        .build();
  }

  public ObjectQuery createObjectQuery(TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {

    return createObjectQuery("", typeConfiguration, environment);
  }

  public ObjectQuery createObjectQuery(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    List<FieldConfiguration> scalarFields = getScalarFields(fieldPathPrefix, typeConfiguration, environment);
    List<ObjectFieldConfiguration> objectFields = getObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<NestedObjectFieldConfiguration> nestedObjectFields =
        getNestedObjectFields(fieldPathPrefix, typeConfiguration, environment);

    return ObjectQuery.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .objectFields(objectFields)
        .nestedObjectFields(nestedObjectFields)
        .build();
  }

  private ObjectQuery createObjectQuery(FieldConfigurationPair pair, DataFetchingEnvironment environment) {
    String fieldPathPrefix = pair.getSelectedField()
        .getFullyQualifiedName()
        .concat("/");
    TypeConfiguration<?> typeConfiguration = pair.getFieldConfiguration()
        .getTypeConfiguration();

    return createObjectQuery(fieldPathPrefix, typeConfiguration, environment);
  }

  private List<FieldConfiguration> getScalarFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> typeConfiguration.getFields()
            .get(selectedField.getName()))
        .filter(AbstractFieldConfiguration::isScalarField)
        .collect(Collectors.toList());
  }

  private List<ObjectFieldConfiguration> getObjectFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
        .filter(pair -> pair.getFieldConfiguration()
            .isObjectField())
        .map(pair -> ObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .objectQuery(createObjectQuery(pair, environment))
            .build())
        .collect(Collectors.toList());
  }

  private List<NestedObjectFieldConfiguration> getNestedObjectFields(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
        .filter(pair -> pair.getFieldConfiguration()
            .isNestedObjectField())
        .map(pair -> NestedObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .scalarFields(getScalarFields(pair.getSelectedField()
                .getFullyQualifiedName()
                .concat("/"),
                pair.getFieldConfiguration()
                    .getTypeConfiguration(),
                environment))
            .build())
        .collect(Collectors.toList());
  }

  private Stream<FieldConfigurationPair> getFieldConfigurationPairs(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> FieldConfigurationPair.builder()
            .selectedField(selectedField)
            .fieldConfiguration(typeConfiguration.getFields()
                .get(selectedField.getName()))
            .build());
  }

  private List<SelectedField> getSelectedFields(String fieldPathPrefix, DataFetchingEnvironment environment) {
    return environment.getSelectionSet()
        .getFields(fieldPathPrefix.concat("*.*"));
  }

  @Data
  @Builder
  private static class FieldConfigurationPair {
    private final SelectedField selectedField;

    private final AbstractFieldConfiguration fieldConfiguration;
  }
}
