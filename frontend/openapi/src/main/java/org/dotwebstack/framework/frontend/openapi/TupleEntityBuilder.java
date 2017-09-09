package org.dotwebstack.framework.frontend.openapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.properties.PropertyHandlerAdapter;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TupleEntityBuilder implements EntityBuilder<TupleQueryResult> {

  private PropertyHandlerAdapter propertyHandler;

  @Autowired
  public TupleEntityBuilder(@NonNull PropertyHandlerAdapter propertyHandler) {
    this.propertyHandler = propertyHandler;
  }

  public Object build(@NonNull TupleQueryResult result, @NonNull Property schema) {
    if (schema instanceof ArrayProperty) {
      Property itemSchema = ((ArrayProperty) schema).getItems();

      if (!(itemSchema instanceof ObjectProperty)) {
        throw new EntityBuilderRuntimeException(
            "Only object properties are supported for array items.");
      }

      ImmutableList.Builder<Map<String, Object>> collectionBuilder = new ImmutableList.Builder<>();
      Map<String, Property> itemProperties = ((ObjectProperty) itemSchema).getProperties();

      while (result.hasNext()) {
        BindingSet bindingSet = result.next();
        ImmutableMap.Builder<String, Object> itemBuilder = new ImmutableMap.Builder<>();

        itemProperties.forEach((name, property) -> {
          if (bindingSet.hasBinding(name)) {
            itemBuilder.put(name, propertyHandler.handle(property, bindingSet.getValue(name)));
          }
        });

        collectionBuilder.add(itemBuilder.build());
      }

      return collectionBuilder.build();
    }

    return ImmutableMap.of();
  }

}
