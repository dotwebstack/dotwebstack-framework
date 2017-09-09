package org.dotwebstack.framework.frontend.openapi.properties;

import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.Property;
import java.math.BigInteger;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class IntegerPropertyHandler implements PropertyHandler<BaseIntegerProperty, BigInteger> {

  @Override
  public BigInteger handle(@NonNull BaseIntegerProperty property, @NonNull Value value) {
    if (!(value instanceof Literal)) {
      throw new PropertyHandlerRuntimeException(
          String.format("Property '%s' is not a literal value.", property.getName()));
    }

    return ((Literal) value).integerValue();
  }

  @Override
  public boolean supports(@NonNull Property property) {
    return BaseIntegerProperty.class.isInstance(property);
  }

}
