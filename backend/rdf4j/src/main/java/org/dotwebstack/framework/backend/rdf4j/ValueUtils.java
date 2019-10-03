package org.dotwebstack.framework.backend.rdf4j;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;

public final class ValueUtils {

  private ValueUtils() {}

  public static IRI findRequiredPropertyIri(@NonNull Model model, @NonNull Resource subject, @NonNull IRI predicate) {
    return Models.getPropertyIRI(model, subject, predicate)
        .orElseThrow(
            () -> invalidConfigurationException("Resource '{}' requires a '{}' IRI property.", subject, predicate));
  }

  public static Set<IRI> findRequiredPropertyIris(@NonNull Model model, @NonNull Resource subject,
      @NonNull IRI predicate) {
    Set<IRI> result = Models.getPropertyIRIs(model, subject, predicate);

    if (isEmpty(result)) {
      throw invalidConfigurationException("Resource '{}' requires a '{}' IRI property.", subject, predicate);
    }

    return result;
  }

  public static Optional<IRI> findOptionalPropertyIri(@NonNull Model model, @NonNull Resource subject,
      @NonNull IRI predicate) {
    return Models.getPropertyIRI(model, subject, predicate);
  }

  public static boolean isPropertyIriPresent(@NonNull Model model, @NonNull Resource subject, @NonNull IRI predicate) {
    try {
      findRequiredPropertyIri(model, subject, predicate);
      return true;
    } catch (InvalidConfigurationException e) {
      return false;
    }
  }

  public static Literal findRequiredPropertyLiteral(@NonNull Model model, @NonNull Resource subject,
      @NonNull IRI predicate) {
    return Models.getPropertyLiteral(model, subject, predicate)
        .orElseThrow(
            () -> invalidConfigurationException("Resource '{}' requires a '{}' literal property.", subject, predicate));
  }

  public static Value findRequiredProperty(@NonNull Model model, @NonNull Resource subject, @NonNull IRI predicate) {
    return Models.getProperty(model, subject, predicate)
        .orElseThrow(
            () -> invalidConfigurationException("Resource '{}' requires a '{}' property.", subject, predicate));
  }

  public static boolean isPropertyPresent(@NonNull Model model, @NonNull Resource subject, @NonNull IRI predicate) {
    try {
      findRequiredProperty(model, subject, predicate);
      return true;
    } catch (InvalidConfigurationException e) {
      return false;
    }
  }
}
