package org.dotwebstack.framework.core.query;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.springframework.stereotype.Component;

@Component
public class QueryFactory {

  // private final DotWebStackConfiguration dotWebStackConfiguration;

  // public QueryFactory(DotWebStackConfiguration dotWebStackConfiguration) {
  // this.dotWebStackConfiguration = dotWebStackConfiguration;
  // }

  public ObjectQuery createObjectQuery(TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {

    List<FieldConfiguration> scalarFields = getScalarFields(typeConfiguration, environment);

    return ObjectQuery.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .build();
  }

  private List<FieldConfiguration> getScalarFields(TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return getSelectedFields(environment).stream()
        .map(selectedField -> typeConfiguration.getFields()
            .get(selectedField.getName()))
        .collect(Collectors.toList());
  }

  private List<SelectedField> getSelectedFields(DataFetchingEnvironment environment) {
    return environment.getSelectionSet()
        .getFields("*.*");
  }
}
