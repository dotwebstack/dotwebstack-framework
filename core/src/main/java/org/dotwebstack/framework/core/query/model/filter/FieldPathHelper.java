package org.dotwebstack.framework.core.query.model.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;

public class FieldPathHelper {

  public static FieldPath createFieldPath(DotWebStackConfiguration dotWebStackConfiguration,
      TypeConfiguration<?> typeConfiguration, String fieldPath) {
    return createFieldPath(typeConfiguration, fieldPath, abstractFieldConfiguration -> dotWebStackConfiguration
        .getTypeConfiguration(abstractFieldConfiguration.getType()));
  }

  public static FieldPath createFieldPath(TypeConfiguration<?> typeConfiguration, String fieldPath) {
    return createFieldPath(typeConfiguration, fieldPath, AbstractFieldConfiguration::getTypeConfiguration);
  }

  public static FieldPath createFieldPath(TypeConfiguration<?> typeConfiguration, String fieldPath,
      Function<AbstractFieldConfiguration, TypeConfiguration<?>> typeProvider) {
    String field = StringUtils.substringBefore(fieldPath, ".");
    String rest = StringUtils.substringAfter(fieldPath, ".");

    return typeConfiguration.getField(field)
        .map(fieldConfiguration -> {
          FieldPath.FieldPathBuilder builder = FieldPath.builder()
              .fieldConfiguration(fieldConfiguration);

          if (!StringUtils.isBlank(rest)) {
            builder.child(createFieldPath(typeProvider.apply(fieldConfiguration), rest, typeProvider));
          }

          return builder.build();
        })
        .orElseThrow(() -> illegalStateException("Invalid field path!"));
  }
}
