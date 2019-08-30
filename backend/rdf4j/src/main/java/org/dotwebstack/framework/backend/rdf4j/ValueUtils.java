package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.Set;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;

public final class ValueUtils {

  private ValueUtils() {}

  public static IRI findRequiredPropertyIri(Model model, Resource subject, IRI predicate) {
    return Models.getPropertyIRI(model, subject, predicate)
        .orElseThrow(
            () -> invalidConfigurationException("Resource '{}' requires a '{}' IRI property.", subject, predicate));
  }

  public static Set<IRI> findRequiredPropertyIris(Model model, Resource subject, IRI predicate) {
    Set<IRI> result = Models.getPropertyIRIs(model, subject, predicate);

    if (isEmpty(result)) {
      throw invalidConfigurationException("Resource '{}' requires a '{}' IRI property.", subject, predicate);
    }

    return result;
  }

  public static boolean isPropertyIriPresent(Model model, Resource subject, IRI predicate) {
    try {
      findRequiredPropertyIri(model, subject, predicate);
      return true;
    } catch (InvalidConfigurationException e) {
      return false;
    }
  }

  public static Literal findRequiredPropertyLiteral(Model model, Resource subject, IRI predicate) {
    return Models.getPropertyLiteral(model, subject, predicate)
        .orElseThrow(
            () -> invalidConfigurationException("Resource '{}' requires a '{}' literal property.", subject, predicate));
  }

  public static Value findRequiredProperty(Model model, Resource subject, IRI predicate) {
    return Models.getProperty(model, subject, predicate)
        .orElseThrow(
            () -> invalidConfigurationException("Resource '{}' requires a '{}' property.", subject, predicate));
  }

  public static boolean isPropertyPresent(Model model, Resource subject, IRI predicate) {
    try {
      findRequiredProperty(model, subject, predicate);
      return true;
    } catch (InvalidConfigurationException e) {
      return false;
    }
  }
}
