package org.dotwebstack.framework.core.query;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.springframework.stereotype.Component;

@Component
public class QueryFactory {

  // private final DotWebStackConfiguration dotWebStackConfiguration;

  // public QueryFactory(DotWebStackConfiguration dotWebStackConfiguration) {
  // this.dotWebStackConfiguration = dotWebStackConfiguration;
  // }

  public ObjectQuery createObjectQuery(TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {

    return createObjectQuery("", typeConfiguration, environment);
  }

  public ObjectQuery createObjectQuery(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {

    List<FieldConfiguration> scalarFields = getScalarFields(fieldPathPrefix, typeConfiguration, environment);
    List<ObjectFieldConfiguration> objectFields = getObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<NestedObjectFieldConfiguration> nestedObjectFields = getNestedObjectFields(fieldPathPrefix, typeConfiguration, environment);

    return ObjectQuery.builder()
            .typeConfiguration(typeConfiguration)
            .scalarFields(scalarFields)
            .objectFields(objectFields)
            .nestedObjectFields(nestedObjectFields)
            .build();
  }

  private List<FieldConfiguration> getScalarFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> typeConfiguration.getFields()
            .get(selectedField.getName()))
        .filter(AbstractFieldConfiguration::isScalarField)
        .collect(Collectors.toList());
  }

  private List<SelectedField> getSelectedFields(String fieldPathPrefix, DataFetchingEnvironment environment) {
    return environment.getSelectionSet()
        .getFields(fieldPathPrefix.concat("*.*"));
  }

  private List<ObjectFieldConfiguration> getObjectFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
                                                         DataFetchingEnvironment environment){

    List<ObjectFieldConfiguration> objectFields = new ArrayList<>();
    for( SelectedField selectedField : getSelectedFields(fieldPathPrefix, environment) ){

      AbstractFieldConfiguration fieldConfiguration = typeConfiguration.getFields().get(selectedField.getName());
      if( fieldConfiguration.isObjectField()){

        String pathPrefix = selectedField.getFullyQualifiedName().concat("/");
        var type = fieldConfiguration.getTypeConfiguration();
        objectFields.add( new ObjectFieldConfiguration(fieldConfiguration, createObjectQuery( pathPrefix, type, environment) ) );
      }
    }

    return objectFields;
  }

  private List<NestedObjectFieldConfiguration> getNestedObjectFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
                                                                     DataFetchingEnvironment environment){

    List<NestedObjectFieldConfiguration> nestedObjectFields = new ArrayList<>();
    for( SelectedField selectedField : getSelectedFields(fieldPathPrefix, environment) ){

      AbstractFieldConfiguration fieldConfiguration = typeConfiguration.getFields().get(selectedField.getName());
      if( fieldConfiguration.isNestedObjectField()){

        String pathPrefix = selectedField.getFullyQualifiedName().concat("/");
        var type = fieldConfiguration.getTypeConfiguration();
        nestedObjectFields.add( new NestedObjectFieldConfiguration(fieldConfiguration, getScalarFields( pathPrefix, type, environment) ) );
      }
    }

    return nestedObjectFields;
  }
}
