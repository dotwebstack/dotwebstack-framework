package org.dotwebstack.framework.backend.postgres.query.model;

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
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectRequest extends ObjectRequest {
  @Builder.Default
  private final Map<String, ObjectFieldConfiguration> objectFieldsByFieldName = new HashMap<>();

  public void addFilterCriteria(List<FilterCriteria> filterCriterias) {
    filterCriterias.stream()
        .filter(FilterCriteria::isNestedFilter)
        .forEach(filterCriteria -> createObjectField(filterCriteria.getFieldPath(), Origin.FILTERING));
  }

  public void addSortCriteria(List<SortCriteria> sortCriterias) {
    sortCriterias.stream()
        .filter(sortCriteria -> !sortCriteria.getFieldPath()
            .isLeaf())
        .forEach(filterCriteria -> createObjectField(filterCriteria.getFieldPath(), Origin.SORTING));
  }

  private void createObjectField(FieldPath fieldPath, Origin origin) {
    var parentTypeConfiguration = (PostgresTypeConfiguration) getTypeConfiguration();
    var fieldConfiguration = parentTypeConfiguration.getFields()
        .get(fieldPath.getFieldConfiguration()
            .getName());
    var typeConfiguration = (PostgresTypeConfiguration) fieldConfiguration.getTypeConfiguration();
    var objectField = Optional.ofNullable(objectFieldsByFieldName.get(fieldConfiguration.getName()))
        .orElseGet(() -> {
          var newObjectField = createObjectFieldConfiguration(fieldConfiguration, typeConfiguration);
          objectFieldsByFieldName.put(fieldConfiguration.getName(), newObjectField);
          objectFields.add(newObjectField);
          return newObjectField;
        });

    if (!fieldPath.isLeaf()) {
      addObjectFields(fieldPath.getChild(), objectField, typeConfiguration, origin);
    }
  }

  private void addObjectFields(FieldPath fieldPath, ObjectFieldConfiguration parentObjectFieldConfiguration,
      PostgresTypeConfiguration parentTypeConfiguration, Origin origin) {
    var fieldConfiguration = parentTypeConfiguration.getFields()
        .get(fieldPath.getFieldConfiguration()
            .getName());
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

      if (!fieldPath.isLeaf()) {
        addObjectFields(fieldPath.getChild(), objectField, typeConfiguration, origin);
      }
    } else if (fieldConfiguration.isScalarField()) {
      fieldConfiguration.addOrigin(origin);
      parentObjectFieldConfiguration.getObjectRequest()
          .addScalarField(fieldConfiguration);
    }
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
