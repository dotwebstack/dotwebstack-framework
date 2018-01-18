package org.dotwebstack.framework.param;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.param.types.TermParameter;
import org.eclipse.rdf4j.model.IRI;

public final class TermParameterDefinition extends AbstractParameterDefinition<TermParameter<?>> {

  private static final PropertyShape DEFAULT_SHAPE = new StringPropertyShape();

  private final Optional<PropertyShape> shapeType;

  public TermParameterDefinition(@NonNull IRI identifier, @NonNull String name,
      @NonNull Optional<PropertyShape> shapeType) {
    super(identifier, name);

    this.shapeType = shapeType;
  }

  @Override
  public TermParameter<?> createOptionalParameter() {
    return createParameter(false);
  }

  @Override
  public TermParameter<?> createRequiredParameter() {
    return createParameter(true);
  }

  private TermParameter<?> createParameter(boolean required) {
    Class<?> parameterClass = shapeType.orElse(DEFAULT_SHAPE).getTermClass();

    Optional<Constructor<?>> constructorOptional =
        Arrays.asList(parameterClass.getConstructors()).stream().filter(
            c -> c.getParameterCount() == 3).findFirst();

    if (constructorOptional.isPresent()) {
      Constructor constructor = constructorOptional.get();

      try {
        return (TermParameter) constructor.newInstance(getIdentifier(), getName(), required);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException(e);
      }
    }

    throw new IllegalStateException(String.format(
        "Zero or more than one constructor found in TermParameter class: '%s'", parameterClass));
  }

}
