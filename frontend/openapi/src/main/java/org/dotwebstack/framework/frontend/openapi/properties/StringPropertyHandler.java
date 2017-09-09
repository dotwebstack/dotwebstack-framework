package org.dotwebstack.framework.frontend.openapi.properties;

import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class StringPropertyHandler implements PropertyHandler<StringProperty, String> {

  @Override
  public String handle(@NonNull StringProperty property, @NonNull Value value) {
    return value.stringValue();
  }

  @Override
  public boolean supports(@NonNull Property property) {
    return StringProperty.class.isInstance(property);
  }

}
