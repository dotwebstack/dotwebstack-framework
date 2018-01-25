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
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
final class TermParameterDefinitionFactory implements ParameterDefinitionFactory {

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
