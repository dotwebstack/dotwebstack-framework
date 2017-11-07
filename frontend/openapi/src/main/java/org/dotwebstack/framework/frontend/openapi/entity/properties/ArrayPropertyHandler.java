package org.dotwebstack.framework.frontend.openapi.entity.properties;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilderContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.OasVendorExtensions;
import org.eclipse.rdf4j.model.Value;
import org.springframework.stereotype.Service;

@Service
public class ArrayPropertyHandler extends AbstractPropertyHandler<ArrayProperty> {

  @Override
  public Object handle(ArrayProperty property, EntityBuilderContext entityBuilderContext,
      PropertyHandlerRegistry registry, Value context) {

    Property itemProperty = property.getItems();
    ImmutableList.Builder<Map<String,Object>> builder = ImmutableList.builder();

    if ("collection".equals(property.getVendorExtensions().get(OasVendorExtensions.RESULT_REF))) {
      for (Value subject : entityBuilderContext.getQueryResult().getSubjects()) {

        Map<String, Object> resource =
            buildResource(itemProperty, entityBuilderContext, registry, subject);

        builder.add(resource);
      }
    } else if (property.getVendorExtensions().containsKey(OasVendorExtensions.LDPATH)) {

      LdPathExecutor ldPathExecutor = entityBuilderContext.getLdPathExecutor();
      Collection<Value> queryResult = ldPathExecutor.ldPathQuery(
          context, (String) property.getVendorExtensions().get(OasVendorExtensions.LDPATH));


//      queryResult.forEach(value -> builder.add(
//          Optional.ofNullable(registry.handle(itemProperty, entityBuilderContext, value))));

    } else {
     // builder.add(new HashMap<String,Object>());
//      throw new PropertyHandlerRuntimeException(
//          String.format("ArrayProperty must have either a '%s', of a '%s' attribute.",
//              OasVendorExtensions.LDPATH, OasVendorExtensions.RESULT_REF));
    }

    return builder.build();
  }

  @Override
  public boolean supports(Property property) {
    return ArrayProperty.class.isInstance(property);
  }


}
