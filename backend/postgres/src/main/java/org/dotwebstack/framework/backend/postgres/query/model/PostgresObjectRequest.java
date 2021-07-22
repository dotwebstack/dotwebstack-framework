package org.dotwebstack.framework.backend.postgres.query.model;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
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

  public void addFields(FilterCriteria criteria) {
    var origin = Origin.filtering(criteria);

    criteria.getFieldPaths()
        .stream()
        .filter(FieldPath::isNode)
        .forEach(fieldPath -> addFields(fieldPath, origin));
  }

  public void addFields(SortCriteria criteria, Map<String, String> fieldPathAliasMap) {
    var origin = Origin.sorting(criteria, fieldPathAliasMap);

    Optional.of(criteria)
        .map(SortCriteria::getFieldPath)
        .filter(FieldPath::isNode)
        .ifPresent(fieldPath -> addFields(fieldPath, origin));
  }

  private void addFields(FieldPath fieldPath, Origin origin) {
    var fieldConfiguration = (PostgresFieldConfiguration) fieldPath.getFieldConfiguration();
    var typeConfiguration = (PostgresTypeConfiguration) fieldConfiguration.getTypeConfiguration();

    if (fieldConfiguration.isNested()) {
      addNestedScalarField(this, fieldPath, origin);
    } else {
      var objectField = getOrCreateObjectField(this, typeConfiguration, fieldConfiguration);

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
      var objectField = getOrCreateObjectField(parentObjectFieldConfiguration.getObjectRequest(), typeConfiguration,
          fieldConfiguration);

      if (!fieldPath.isLeaf()) {
        addObjectFields(fieldPath.getChild(), objectField, origin);
      }
    }

    if (fieldConfiguration.isNestedObjectField()) {
      addNestedScalarField(parentObjectFieldConfiguration.getObjectRequest(), fieldPath, origin);
    }

    if (fieldConfiguration.isScalarField()) {
      addScalarField(parentObjectFieldConfiguration.getObjectRequest()
          .getScalarFields(), fieldConfiguration, origin);
    }
  }

  private void addNestedScalarField(ObjectRequest objectRequest, FieldPath fieldPath, Origin origin) {
    var fieldConfiguration = (PostgresFieldConfiguration) fieldPath.getFieldConfiguration();

    if (fieldConfiguration.isList()) {
      throw unsupportedOperationException("Nested object list is unsupported!");
    }

    var nestedObjectField = getOrCreateNestedObjectField(objectRequest.getNestedObjectFields(), fieldConfiguration);

    var scalarFieldConfiguration = fieldPath.getChild()
        .getFieldConfiguration();

    addScalarField(nestedObjectField.getScalarFields(), scalarFieldConfiguration, origin);
  }

  private void addScalarField(List<ScalarField> scalarFields, FieldConfiguration fieldConfiguration, Origin origin) {
    var scalarField = scalarFields.stream()
        .filter(sf -> Objects.equals(sf.getField(), fieldConfiguration))
        .findFirst()
        .orElseGet(() -> {
          var newScalarField = ScalarField.builder()
              .field(fieldConfiguration)
              .build();
          scalarFields.add(newScalarField);
          return newScalarField;
        });

    scalarField.addOrigin(origin);
  }

  private NestedObjectFieldConfiguration getOrCreateNestedObjectField(
      List<NestedObjectFieldConfiguration> nestedObjectFieldConfigurations,
      PostgresFieldConfiguration fieldConfiguration) {
    return nestedObjectFieldConfigurations.stream()
        .filter(nestedObjectFieldConfiguration -> Objects.equals(nestedObjectFieldConfiguration.getField(),
            fieldConfiguration))
        .findFirst()
        .orElseGet(() -> {
          var newNestedObjectField = NestedObjectFieldConfiguration.builder()
              .field(fieldConfiguration)
              .build();

          nestedObjectFieldConfigurations.add(newNestedObjectField);
          return newNestedObjectField;
        });
  }

  private ObjectFieldConfiguration getOrCreateObjectField(ObjectRequest objectRequest,
      PostgresTypeConfiguration typeConfiguration, PostgresFieldConfiguration fieldConfiguration) {
    return objectRequest.getObjectField(fieldConfiguration)
        .orElseGet(() -> {
          var newObjectField = createObjectFieldConfiguration(typeConfiguration, fieldConfiguration);
          objectRequest.getObjectFields()
              .add(newObjectField);
          return newObjectField;
        });
  }

  private static ObjectFieldConfiguration createObjectFieldConfiguration(
      TypeConfiguration<? extends FieldConfiguration> typeConfiguration,
      AbstractFieldConfiguration fieldConfiguration) {
    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .build();

    return ObjectFieldConfiguration.builder()
        .field(fieldConfiguration)
        .objectRequest(objectRequest)
        .build();
  }
}
