package org.dotwebstack.framework.backend.postgres.query.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectRequest extends ObjectRequest {
  @Builder.Default
  private final Map<String, ObjectFieldConfiguration> objectFieldsByType = new HashMap<>();

  public void addFilterCriteria(List<FilterCriteria> filterCriterias) {
    filterCriterias.stream()
        .filter(FilterCriteria::isNestedFilter)
        .forEach(filterCriteria -> {
          var mainTypeConfiguration = (PostgresTypeConfiguration) getTypeConfiguration();
          createObjectField(filterCriteria.getFieldPath(), mainTypeConfiguration, Origin.FILTERING);
        });
  }

  private ObjectFieldConfiguration createObjectField(String[] fieldPaths,
      PostgresTypeConfiguration parentTypeConfiguration, Origin origin) {
    var fieldConfiguration = parentTypeConfiguration.getFields()
        .get(fieldPaths[0]);
    var typeConfiguration = (PostgresTypeConfiguration) fieldConfiguration.getTypeConfiguration();
    var objectField = Optional.ofNullable(objectFieldsByType.get(fieldConfiguration.getName()))
        .orElseGet(() -> {
          var newObjectField = createObjectFieldConfiguration(fieldConfiguration, typeConfiguration);
          objectFieldsByType.put(fieldConfiguration.getType(), newObjectField);
          objectFields.add(newObjectField);
          return newObjectField;
        });

    fieldPaths = Arrays.copyOfRange(fieldPaths, 1, fieldPaths.length);
    addObjectFields(fieldPaths, objectField, typeConfiguration, origin);
    return objectField;
  }

  private ObjectFieldConfiguration addObjectFields(String[] fieldPaths,
      ObjectFieldConfiguration parentObjectFieldConfiguration, PostgresTypeConfiguration parentTypeConfiguration,
      Origin origin) {
    var fieldConfiguration = parentTypeConfiguration.getFields()
        .get(fieldPaths[0]);
    if (fieldConfiguration.isObjectField()) {
      var typeConfiguration = (PostgresTypeConfiguration) fieldConfiguration.getTypeConfiguration();
      var objectField = parentObjectFieldConfiguration.getObjectRequest()
          .getObjectField(fieldConfiguration)
          .orElseGet(() -> {
            var newObjectField = createObjectFieldConfiguration(fieldConfiguration, typeConfiguration);
            parentObjectFieldConfiguration.getObjectRequest()
                .getObjectFields()
                .add(newObjectField);
            return newObjectField;
          });

      fieldPaths = Arrays.copyOfRange(fieldPaths, 1, fieldPaths.length);
      addObjectFields(fieldPaths, objectField, typeConfiguration, origin);
    } else if (fieldConfiguration.isScalarField()) {
      fieldConfiguration.addOrigin(origin);
      parentObjectFieldConfiguration.getObjectRequest()
          .addScalarField(fieldConfiguration);
    }
    return parentObjectFieldConfiguration;
  }

  private ObjectFieldConfiguration createObjectFieldConfiguration(PostgresFieldConfiguration fieldConfiguration,
      PostgresTypeConfiguration typeConfiguration) {
    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .build();

    return ObjectFieldConfiguration.builder()
        .field(fieldConfiguration)
        .objectRequest(objectRequest)
        .build();
  }
}
