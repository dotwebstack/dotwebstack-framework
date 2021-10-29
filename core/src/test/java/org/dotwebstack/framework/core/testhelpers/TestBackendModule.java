package org.dotwebstack.framework.core.testhelpers;


import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.backend.BackendLoaderFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

public class TestBackendModule implements BackendModule<TestObjectType> {
  private final TestBackendLoaderFactory backendLoaderFactory;

  public TestBackendModule(TestBackendLoaderFactory backendLoaderFactory) {
    this.backendLoaderFactory = backendLoaderFactory;
  }

  @Override
  public Class<TestObjectType> getObjectTypeClass() {
    return TestObjectType.class;
  }

  @Override
  public BackendLoaderFactory getBackendLoaderFactory() {
    return backendLoaderFactory;
  }

  @Override
  public void init(Map<String, ObjectType<? extends ObjectField>> objectTypes) {
    var postgresObjectTypes = objectTypes.values()
        .stream()
        .map(TestObjectType.class::cast)
        .collect(Collectors.toList());

    var allFields = postgresObjectTypes.stream()
        .flatMap(objectType -> objectType.getFields()
            .values()
            .stream())
        .collect(Collectors.toList());

    allFields.stream()
        .filter(objectField -> objectTypes.containsKey(objectField.getType()))
        .forEach(objectField -> {
          var targetType = (TestObjectType) objectTypes.get(objectField.getType());
          objectField.setTargetType(targetType);
        });

    // mapped By
    allFields.stream()
        .filter(objectField -> isNotEmpty(objectField.getMappedBy()))
        .forEach(objectField -> {
          var type = StringUtils.isNotEmpty(objectField.getAggregationOf()) ? objectField.getAggregationOf()
              : objectField.getType();

          var objectType = objectTypes.get(type);

          TestObjectField mappedByObjectField = (TestObjectField) objectType.getFields()
              .get(objectField.getMappedBy());

          objectField.setMappedByObjectField(mappedByObjectField);
        });

    // aggregation
    allFields.stream()
        .filter(AggregateHelper::isAggregate)
        .forEach(objectField -> {
          var aggregationOfType = (TestObjectType) objectTypes.get(objectField.getAggregationOf());

          objectField.setAggregationOfType(aggregationOfType);
        });
  }
}
