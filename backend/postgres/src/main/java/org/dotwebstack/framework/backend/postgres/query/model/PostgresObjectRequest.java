package org.dotwebstack.framework.backend.postgres.query.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@Data
public class PostgresObjectRequest extends ObjectRequest {
  // TODO exclude from getter and setter
  @Builder.Default
  private final Map<String, PostgresObjectFieldConfiguration> postgresObjectFieldsByType = new HashMap<>();

  @Builder.Default
  private final List<PostgresObjectFieldConfiguration> postgresObjectFields = new ArrayList<>();

  public void addFilterCriteria(List<FilterCriteria> filterCriterias) {
    filterCriterias.stream().filter(FilterCriteria::isCompositeFilter).forEach(filterCriteria -> {
      var mainTypeConfiguration =  (PostgresTypeConfiguration) getTypeConfiguration();
      createPostgresObjectFieldConfiguration(filterCriteria.getFilterFields(), mainTypeConfiguration);
    });
  }

  private void addPostgresObjectField(PostgresObjectFieldConfiguration postgresObjectField) {
    postgresObjectFields.add(postgresObjectField);
  }

  private PostgresObjectFieldConfiguration createPostgresObjectFieldConfiguration(String[] filterFields, PostgresTypeConfiguration typeConfiguration){
    var filterFieldConfiguration = typeConfiguration.getFields().get(filterFields[0]);
    var filterObjectFieldConfiguration = postgresObjectFieldsByType.get(filterFieldConfiguration.getType());
    var filterTypeConfiguration = (PostgresTypeConfiguration)filterFieldConfiguration.getTypeConfiguration();
    // TODO use optional
    if(filterObjectFieldConfiguration == null){
      // TODO: should we add a keyfield as scalar to the objectRequest?
      var newPostgresObjectRequest = PostgresObjectRequest.builder().typeConfiguration(filterTypeConfiguration).build();
      filterObjectFieldConfiguration = PostgresObjectFieldConfiguration.builder()
          .origin(Origin.FILTERING)
          .field(filterFieldConfiguration)
          .postgresObjectRequest(newPostgresObjectRequest).build();
      postgresObjectFieldsByType.put(filterFieldConfiguration.getType(), filterObjectFieldConfiguration);
      postgresObjectFields.add(filterObjectFieldConfiguration);
    }
    filterFields = Arrays.copyOfRange(filterFields, 1, filterFields.length);
    createPostgresObjectFieldConfiguration(filterFields, filterObjectFieldConfiguration, filterTypeConfiguration);
    return filterObjectFieldConfiguration;
  }

  private PostgresObjectFieldConfiguration createPostgresObjectFieldConfiguration(String[] filterFields, PostgresObjectFieldConfiguration postgresObjectFieldConfiguration, PostgresTypeConfiguration typeConfiguration){
    var field = filterFields[0];
    var fieldConfiguration = typeConfiguration.getFields().get(field);
    if(fieldConfiguration.isObjectField()) {
      var newTypeConfiguration = (PostgresTypeConfiguration)fieldConfiguration.getTypeConfiguration();
      var newPostgresObjectRequest = PostgresObjectRequest.builder().typeConfiguration(typeConfiguration).build();
      var newPostgresObjectFieldConfiguration = PostgresObjectFieldConfiguration.builder()
          .origin(Origin.FILTERING)
          .field(fieldConfiguration)
          .postgresObjectRequest(newPostgresObjectRequest).build();

      postgresObjectFieldConfiguration.getPostgresObjectRequest().addPostgresObjectField(newPostgresObjectFieldConfiguration);
      filterFields = Arrays.copyOfRange(filterFields, 1, filterFields.length);
      createPostgresObjectFieldConfiguration(filterFields, newPostgresObjectFieldConfiguration, newTypeConfiguration);
    }else if(fieldConfiguration.isScalarField()){
      // add scalar
      // TODO: if scalarfield already exists, create a new fieldconfiguration or add origin to fieldconfiguration
      fieldConfiguration.setOrigin(Origin.FILTERING);
      postgresObjectFieldConfiguration.getPostgresObjectRequest().getScalarFields().add(fieldConfiguration);
    }
    return postgresObjectFieldConfiguration;
  }
}
