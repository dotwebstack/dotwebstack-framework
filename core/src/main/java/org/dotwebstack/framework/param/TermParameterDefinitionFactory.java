package org.dotwebstack.framework.param;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.shapes.BooleanPropertyShape;
import org.dotwebstack.framework.param.shapes.IntegerPropertyShape;
import org.dotwebstack.framework.param.shapes.IriPropertyShape;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.stereotype.Service;

@Service
final class TermParameterDefinitionFactory implements ParameterDefinitionFactory {

  private Set<PropertyShape> supportedShapes = new HashSet<>();

  public TermParameterDefinitionFactory() {
    supportedShapes.add(new StringPropertyShape());
    supportedShapes.add(new IntegerPropertyShape());
    supportedShapes.add(new BooleanPropertyShape());
    supportedShapes.add(new IriPropertyShape());
  }

  @Override
  public TermParameterDefinition create(@NonNull Model model, @NonNull IRI id) {
    String name = Models.objectLiteral(model.filter(id, ELMO.NAME_PROP, null)).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> property found for <%s> of type <%s>", ELMO.NAME_PROP, id,
                ELMO.TERM_FILTER))).stringValue();

    Set<Value> objects = model.filter(id, ELMO.SHAPE_PROP, null).objects();
    Optional<PropertyShape> propertyShapeOptional = Optional.empty();
    if (objects.iterator().hasNext()) {
      Set<Value> iriShapeTypes =
          model.filter((Resource) objects.iterator().next(), null, null).objects();

      propertyShapeOptional = supportedShapes.stream().filter(
          propertyShape -> iriShapeTypes.iterator().next().stringValue().equals(
              propertyShape.getDataType().stringValue())).findFirst();
    }

    return new TermParameterDefinition(id, name, propertyShapeOptional);
  }

  @Override
  public boolean supports(@NonNull IRI type) {
    return type.equals(ELMO.TERM_FILTER);
  }

}
