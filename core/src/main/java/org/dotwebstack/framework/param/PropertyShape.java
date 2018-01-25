package org.dotwebstack.framework.param;

import java.util.Collection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

// XXX (PvH) Waarom gebruiken we hier een static factory method?
@RequiredArgsConstructor(staticName = "of")

// XXX (PvH) Wat vinden we van de naam PropertyShape? Is ShaclShape niet duidelijker? Indien
// akkoord, vergeet niet de parameter namen aan te passen (bijvoorbeeld shapeType -> shaclShape)
public final class PropertyShape {

  @Getter
  @NonNull
  private final IRI datatype;

  // XXX (PvH) Zou een Optional<Value> hier niet praktischer zijn? Bijvoorbeeld in het gebruik in de
  // TermParameterFactory
  @Getter
  private final Value defaultValue;

  // XXX (PvH) Ik loop wat vooruit op de zaken, maar de enum waarden mogen niet null zijn (wel een
  // lege lijst). Dit is in het gebruik makkelijker (en gemeengoed bij collecties): zo hoef je geen
  // null check te doen, en itereren over een lege collectie is geen probleem.
  @Getter
  private final Collection<Value> in;
}
