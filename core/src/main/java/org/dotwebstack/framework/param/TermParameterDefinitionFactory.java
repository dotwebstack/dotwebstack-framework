package org.dotwebstack.framework.param;

import static org.eclipse.rdf4j.model.util.Models.object;
import static org.eclipse.rdf4j.model.util.Models.objectIRI;
import static org.eclipse.rdf4j.model.util.Models.objectResource;

import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.SHACL;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
final class TermParameterDefinitionFactory implements ParameterDefinitionFactory {

  private static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
  // XXX (PvH) PropertyShapes bevatten nu state (de default value). We kunnen ze daarom niet meer
  // als constante behandelen. Nu kan dezelfde StringPropertyShape op 1 moment geen default value
  // hebben, en op een ander moment wel een default value. Dit is vragen om problemen (denk aan
  // threading).
  // Dit is overigens niets ten nadele van jou, ik besef me dit nu ook pas :-)
  //
  // Voorstel:
  //
  // 1. Een concrete implementatie van de PropertyShape. Zie PropertyShape2 als voorbeeld. De shapes
  // in de shapes package kan hiermee vervallen, evenals de AbstractPropertyShape.
  // 2. De nieuwe TermParameterFactory maakt de TermParameters obv de PropertyShape. Zie
  // TermParameterFactory voor het skelet (door jou in te vullen). Zie TermParameterFactoryTest voor
  // de testen.
  // 3. De TermParameterDefinition kan de nieuwe TermParameterFactory gebruiken voor het maken van
  // TermParameters obv de PropertyShape. De constructie met reflectie vervalt hiermee.

  @Override
  public ParameterDefinition create(@NonNull Model model, @NonNull IRI id) {
    String name = Models.objectLiteral(model.filter(id, ELMO.NAME_PROP, null)).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> property found for <%s> of type <%s>", ELMO.NAME_PROP, id,
                ELMO.TERM_FILTER))).stringValue();

    Optional<Resource> shapeObject = objectResource(model.filter(id, ELMO.SHAPE_PROP, null));

    Resource subj = shapeObject.orElseThrow(() -> new ConfigurationException(
        String.format("No <%s> property found for <%s> of type <%s>", ELMO.SHAPE_PROP, id,
            ELMO.TERM_FILTER)));
    IRI iriShapeType = objectIRI(model.filter(subj, SHACL.DATATYPE, null)).orElse(null);
    Value defaultValue = object(model.filter(subj, SHACL.DEFAULT_VALUE, null)).orElse(null);

    PropertyShape shape = PropertyShape.of(iriShapeType, defaultValue, null);
    return new TermParameterDefinition(id, name, shape);
  }

  /**
   * @return {@code true} if {@link ELMO#TERM_FILTER} is supplied; {@code false} otherwise.
   */
  @Override
  public boolean supports(@NonNull IRI type) {
    return type.equals(ELMO.TERM_FILTER);
  }

}
