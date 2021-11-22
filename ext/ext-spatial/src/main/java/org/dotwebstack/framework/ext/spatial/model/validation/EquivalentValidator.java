package org.dotwebstack.framework.ext.spatial.model.validation;


import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;

public class EquivalentValidator implements ConstraintValidator<ValidEquivalent, Map<Integer, SpatialReferenceSystem>> {

  private static final Integer TWO_DIMENSIONAL = 2;

  private static final Integer THREE_DIMENSIONAL = 3;

  @Override
  public boolean isValid(Map<Integer, SpatialReferenceSystem> spatialReferenceSystems,
      ConstraintValidatorContext context) {
    return Optional.ofNullable(spatialReferenceSystems)
        .map(this::isValid)
        .orElse(true);
  }

  private boolean isValid(Map<Integer, SpatialReferenceSystem> spatialReferenceSystems) {
    return spatialReferenceSystems.values()
        .stream()
        .filter(Objects::nonNull)
        .filter(srs -> Objects.nonNull(srs.getEquivalent()))
        .allMatch(srs -> hasValidEquivalent(spatialReferenceSystems, srs));
  }

  private boolean hasValidEquivalent(Map<Integer, SpatialReferenceSystem> spatialReferenceSystems,
      SpatialReferenceSystem srs) {
    SpatialReferenceSystem equivalentSrs = spatialReferenceSystems.get(srs.getEquivalent());

    return hasThreeDimensions(srs) && equivalentSrs != null && hasTwoDimensions(equivalentSrs);
  }

  private boolean hasTwoDimensions(SpatialReferenceSystem srs) {
    return TWO_DIMENSIONAL.equals(srs.getDimensions());
  }

  private boolean hasThreeDimensions(SpatialReferenceSystem srs) {
    return THREE_DIMENSIONAL.equals(srs.getDimensions());
  }
}
