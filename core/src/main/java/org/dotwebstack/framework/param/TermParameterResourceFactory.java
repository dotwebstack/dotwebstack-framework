package org.dotwebstack.framework.param;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
public class TermParameterResourceFactory implements ParameterResourceFactory {

  private Set<PropertyShape> supportedShapes = new HashSet<>();

  public TermParameterResourceFactory() {
    supportedShapes.add(new StringPropertyShape());
    supportedShapes.add(new IntegerPropertyShape());
    supportedShapes.add(new BooleanPropertyShape());
    supportedShapes.add(new IriPropertyShape());
  }

  @Override
  public TermParameterDefinition create(Model model, IRI identifier) {
    String name = getObjectString(model, identifier, ELMO.NAME_PROP).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> property found for <%s> of type <%s>", ELMO.NAME_PROP,
                identifier, ELMO.TERM_FILTER)));

    Set<Value> objects = model.filter(identifier, ELMO.SHAPE_PROP, null).objects();
    Optional<PropertyShape> propertyShapeOptional = Optional.empty();
    if (objects.iterator().hasNext()) {
      Set<Value> iriShapeTypes =
          model.filter((Resource) objects.iterator().next(), null, null).objects();

      propertyShapeOptional = supportedShapes.stream().filter(
          propertyShape -> iriShapeTypes.iterator().next().stringValue().equals(
              propertyShape.getDataType().stringValue())).findFirst();
    }

    return new TermParameterDefinition(identifier, name, propertyShapeOptional);
  }

  @Override
  public boolean supports(IRI backendType) {
    return backendType.equals(ELMO.TERM_FILTER);
  }

  protected Optional<String> getObjectString(Model model, IRI subject, IRI predicate) {
    return Models.objectString(model.filter(subject, predicate, null));
  }


}
