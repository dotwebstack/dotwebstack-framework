package org.dotwebstack.framework.ext.spatial.model.validation;

import java.util.Map;
import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;

public class EquivalentValidator implements ConstraintValidator<ValidEquivalent, Map<Integer, SpatialReferenceSystem>> {

  private static final Integer TWO_DIMENSIONAL = 2;

  private static final Integer THREE_DIMENSIONAL = 3;

  @Override
  public boolean isValid(Map<Integer, SpatialReferenceSystem> spatialReferenceSystems,
      ConstraintValidatorContext context) {
    return spatialReferenceSystems.values()
        .stream()
        .filter(Objects::nonNull)
        .filter(srs -> srs.getEquivalent() != null)
        .allMatch(srs -> hasValidEquivalent(spatialReferenceSystems, srs));
  }

  private boolean hasValidEquivalent(Map<Integer, SpatialReferenceSystem> spatialReferenceSystems,
      SpatialReferenceSystem srs) {
    return hasThreeDimensions(srs) && spatialReferenceSystems.containsKey(srs.getEquivalent())
        && hasTwoDimensions(spatialReferenceSystems.get(srs.getEquivalent()));
  }

  private boolean hasTwoDimensions(SpatialReferenceSystem srs) {
    return TWO_DIMENSIONAL.equals(srs.getDimensions());
  }

  private boolean hasThreeDimensions(SpatialReferenceSystem srs) {
    return THREE_DIMENSIONAL.equals(srs.getDimensions());
  }
}
