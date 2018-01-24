package org.dotwebstack.framework.param;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.param.types.TermParameter;
import org.eclipse.rdf4j.model.IRI;

// XXX (PvH) Ik vraag me af of de typing op de TermParameterDefinition nog stand kan / hoeft te
// houden
public final class TermParameterDefinition<T>
    extends AbstractParameterDefinition<TermParameter<?>> {

  private static final PropertyShape DEFAULT_SHAPE = new StringPropertyShape();

  private final Optional<PropertyShape<T>> shapeType;

  public TermParameterDefinition(@NonNull IRI identifier, @NonNull String name,
      @NonNull Optional<PropertyShape<T>> shapeType) {
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
        Arrays.stream(parameterClass.getConstructors()).filter(
            c -> c.getParameterCount() == 4).findFirst();

    if (constructorOptional.isPresent()) {
      Constructor constructor = constructorOptional.get();

      try {
        T defaultValue = null;
        if (shapeType.isPresent()) {
          defaultValue = shapeType.get().getDefaultValue();
        }
        return (TermParameter) constructor.newInstance(getIdentifier(), getName(), required,
            defaultValue);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException(e);
      }
    }

    throw new IllegalStateException(String.format(
        "Zero or more than one constructor found in TermParameter class: '%s'", parameterClass));
  }

}
