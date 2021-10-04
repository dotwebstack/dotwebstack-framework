package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("test")
public class TypeConfigurationImpl extends AbstractTypeConfiguration<FieldConfigurationImpl> {

  @Override
  public void init(DotWebStackConfiguration dotWebStackConfiguration) {
    initObjectTypes(dotWebStackConfiguration.getObjectTypes());
  }

  @Override
  public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
    return null;
  }

  @Override
  public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
    return null;
  }

  @Override
  public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
    return null;
  }

  private void initObjectTypes(Map<String, AbstractTypeConfiguration<?>> objectTypes) {
    fields.values()
        .stream()
        .filter(FieldConfigurationImpl::isObjectField)
        .forEach(fieldConfiguration -> {

          TypeConfigurationImpl typeConfiguration =
              (TypeConfigurationImpl) objectTypes.get(fieldConfiguration.getType());
          fieldConfiguration.setTypeConfiguration(typeConfiguration);
        });
  }
}
