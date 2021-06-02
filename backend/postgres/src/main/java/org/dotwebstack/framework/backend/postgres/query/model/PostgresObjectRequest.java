package org.dotwebstack.framework.backend.postgres.query.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@Data
public class PostgresObjectRequest extends ObjectRequest {
  // TODO exclude from getter and setter
  @Builder.Default
  private final Map<String, ObjectFieldConfiguration> objectFieldsByType = new HashMap<>();

  public void addFilterCriteria(List<FilterCriteria> filterCriterias) {
    filterCriterias.stream().filter(FilterCriteria::isCompositeFilter).forEach(filterCriteria -> {
      var mainTypeConfiguration =  (PostgresTypeConfiguration) getTypeConfiguration();
      createObjectField(filterCriteria.getFieldPath(), mainTypeConfiguration);
    });
  }

  private ObjectFieldConfiguration createObjectField(String[] filterFields, PostgresTypeConfiguration typeConfiguration){
    var filterFieldConfiguration = typeConfiguration.getFields().get(filterFields[0]);
    var filterObjectFieldConfiguration = objectFieldsByType.get(filterFieldConfiguration.getName());
    var filterTypeConfiguration = (PostgresTypeConfiguration)filterFieldConfiguration.getTypeConfiguration();
    // TODO use optional
    if(filterObjectFieldConfiguration == null){
      // TODO: should we add a keyfield as scalar to the objectRequest?
      var newObjectRequest = ObjectRequest.builder().typeConfiguration(filterTypeConfiguration).build();
      filterObjectFieldConfiguration = ObjectFieldConfiguration.builder()
          .field(filterFieldConfiguration)
          .objectRequest(newObjectRequest).build();
      objectFieldsByType.put(filterFieldConfiguration.getType(), filterObjectFieldConfiguration);
      objectFields.add(filterObjectFieldConfiguration);
    }
    filterFields = Arrays.copyOfRange(filterFields, 1, filterFields.length);
    createObjectField(filterFields, filterObjectFieldConfiguration, filterTypeConfiguration);
    return filterObjectFieldConfiguration;
  }

  private ObjectFieldConfiguration createObjectField(String[] filterFields,ObjectFieldConfiguration objectFieldConfiguration, PostgresTypeConfiguration typeConfiguration){
    var field = filterFields[0];
    var fieldConfiguration = typeConfiguration.getFields().get(field);
    if(fieldConfiguration.isObjectField()) {
      var newTypeConfiguration = (PostgresTypeConfiguration)fieldConfiguration.getTypeConfiguration();
      var newPostgresObjectRequest = ObjectRequest.builder().typeConfiguration(typeConfiguration).build();
      var newPostgresObjectFieldConfiguration = ObjectFieldConfiguration.builder()
          .field(fieldConfiguration)
          .objectRequest(newPostgresObjectRequest).build();

      objectFieldConfiguration.getObjectRequest().getObjectFields().add(newPostgresObjectFieldConfiguration);
      filterFields = Arrays.copyOfRange(filterFields, 1, filterFields.length);
      createObjectField(filterFields, newPostgresObjectFieldConfiguration, newTypeConfiguration);
    }else if(fieldConfiguration.isScalarField()){
      // add scalar
      // TODO: if scalarfield already exists, create a new fieldconfiguration or add origin to fieldconfiguration
      fieldConfiguration.addOrigin(Origin.FILTERING);
      objectFieldConfiguration.getObjectRequest().addScalarField(fieldConfiguration);
    }
    return objectFieldConfiguration;
  }
}
