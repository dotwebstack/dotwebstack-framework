package org.dotwebstack.framework.backend.postgres.query.model;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.ScalarField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPath;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.origin.Origin;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObjectRequest extends ObjectRequest {
  @Builder.Default
  private final Map<String, ObjectFieldConfiguration> objectFieldsByFieldName = new HashMap<>();

  public void addFilterCriteria(List<FilterCriteria> filterCriterias) {
    filterCriterias.stream()
        .filter(FilterCriteria::isNestedFilter)
        .forEach(filterCriteria -> createObjectField(filterCriteria.getFieldPath(), Origin.filtering(filterCriteria)));
  }

  public void addSortCriteria(List<SortCriteria> sortCriterias) {
    sortCriterias.stream()
        .filter(sortCriteria -> !sortCriteria.getFieldPath()
            .isLeaf())
        .forEach(filterCriteria -> createObjectField(filterCriteria.getFieldPath(), Origin.sorting()));
  }

  private void createObjectField(FieldPath fieldPath, Origin origin) {
    var fieldConfiguration = (PostgresFieldConfiguration) fieldPath.getFieldConfiguration();
    var typeConfiguration = (PostgresTypeConfiguration) fieldConfiguration.getTypeConfiguration();

    if (fieldConfiguration.isNested()) {
      if (fieldConfiguration.isList()) {
        throw unsupportedOperationException("Nested object list is unsupported!");
      }

      NestedObjectFieldConfiguration nestedObjectField = nestedObjectFields.stream()
          .filter(nof -> Objects.equals(nof.getField()
              .getName(), fieldConfiguration.getName()))
          .findFirst()
          .orElseGet(() -> {
            NestedObjectFieldConfiguration nofc = NestedObjectFieldConfiguration.builder()
                .field(fieldConfiguration)
                .build();

            nestedObjectFields.add(nofc);

            return nofc;
          });

      nestedObjectField.getScalarFields()
          .add(ScalarField.builder()
              .field(fieldPath.getChild()
                  .getFieldConfiguration())
              .origins(Sets.newHashSet(origin))
              .build());

    } else {
      var objectField = Optional.ofNullable(objectFieldsByFieldName.get(fieldConfiguration.getName()))
          .orElseGet(() -> {
            var newObjectField = createObjectFieldConfiguration(fieldConfiguration, typeConfiguration);
            objectFieldsByFieldName.put(fieldConfiguration.getName(), newObjectField);
            objectFields.add(newObjectField);
            return newObjectField;
          });

      if (!fieldPath.isLeaf()) {
        addObjectFields(fieldPath.getChild(), objectField, origin);
      }
    }
  }

  private void addObjectFields(FieldPath fieldPath, ObjectFieldConfiguration parentObjectFieldConfiguration,
      Origin origin) {
    var fieldConfiguration = (PostgresFieldConfiguration) fieldPath.getFieldConfiguration();
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
        addObjectFields(fieldPath.getChild(), objectField, origin);
      }
    } else if (fieldConfiguration.isScalarField()) {
      var scalarField = parentObjectFieldConfiguration.getObjectRequest()
          .getScalarField(fieldConfiguration)
          .orElseGet(() -> {
            var newScalarField = ScalarField.builder()
                .field(fieldConfiguration)
                .origins(Sets.newHashSet(Origin.requested()))
                .build();
            parentObjectFieldConfiguration.getObjectRequest()
                .addScalarField(newScalarField);
            return newScalarField;
          });

      scalarField.addOrigin(origin);
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
