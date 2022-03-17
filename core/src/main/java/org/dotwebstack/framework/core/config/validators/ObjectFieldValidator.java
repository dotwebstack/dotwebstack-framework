package org.dotwebstack.framework.core.config.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.model.AbstractObjectField;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class ObjectFieldValidator implements SchemaValidator {

  private final CustomValueFetcherDispatcher customValueFetcherDispatcher;

  public ObjectFieldValidator(@Nullable CustomValueFetcherDispatcher customValueFetcherDispatcher) {
    this.customValueFetcherDispatcher = customValueFetcherDispatcher;
  }

  @Override
  public void validate(Schema schema) {
    schema.getObjectTypes()
        .values()
        .forEach(objectType -> validate((ObjectType<AbstractObjectField>) objectType));
  }

  private void validate(ObjectType<AbstractObjectField> objectType) {
    objectType.getFields()
        .values()
        .forEach(objectField -> validate(objectType, objectField));
  }

  private void validate(ObjectType<?> objectType, ObjectField objectField) {
    if (StringUtils.isBlank(objectField.getValueFetcher())) {
      return;
    }

    if (customValueFetcherDispatcher == null) {
      throw invalidConfigurationException(
          "ValueFetcher '{}' configured for field {}.{} but 'CustomValueFetcherDispatcher' bean is missing!",
          objectField.getValueFetcher(), objectType.getName(), objectField.getName());
    }

    if (!customValueFetcherDispatcher.supports(objectField.getValueFetcher())) {
      throw invalidConfigurationException("ValueFetcher '{}' is not supported for field {}.{}!",
          objectField.getValueFetcher(), objectType.getName(), objectField.getName());
    }

    Class<?> resultType = customValueFetcherDispatcher.getResultType(objectField.getValueFetcher());

    if (!resultType.getSimpleName()
        .equalsIgnoreCase(objectField.getType())) {
      throw invalidConfigurationException(
          "Valuefetcher '{}' configured with type '{}' but implementation type is '{}'!", objectField.getValueFetcher(),
          objectField.getType(), resultType.getName());
    }
  }
}
