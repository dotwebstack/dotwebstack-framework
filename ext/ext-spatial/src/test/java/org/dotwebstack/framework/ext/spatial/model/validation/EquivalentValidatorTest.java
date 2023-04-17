package org.dotwebstack.framework.ext.spatial.model.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.dotwebstack.framework.ext.spatial.testhelper.TestSpatialReferenceSystem;
import org.junit.jupiter.api.Test;

class EquivalentValidatorTest {

  private final EquivalentValidator validator = new EquivalentValidator();

  @Test
  void isValid_returnsTrue_forSrsWithoutEquivalent() {
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    TestSpatialReferenceSystem srs = new TestSpatialReferenceSystem();
    srs.setDimensions(2);

    Map<Integer, SpatialReferenceSystem> spatialReferenceSystems = Map.of(1, srs);

    assertTrue(validator.isValid(spatialReferenceSystems, context));
  }

  @Test
  void isValid_returnsTrue_forSrsWithEquivalent() {
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    TestSpatialReferenceSystem srsEquivalent = createSrs(2, null);
    TestSpatialReferenceSystem srs = createSrs(3, 2);

    Map<Integer, SpatialReferenceSystem> spatialReferenceSystems = Map.of(1, srs, 2, srsEquivalent);

    assertTrue(validator.isValid(spatialReferenceSystems, context));
  }

  @Test
  void isValid_returnsFalse_forSrsWithIncorrectSrsDimension() {
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    TestSpatialReferenceSystem srsEquivalent = createSrs(2, null);
    TestSpatialReferenceSystem srs = createSrs(2, 2);

    Map<Integer, SpatialReferenceSystem> spatialReferenceSystems = Map.of(1, srs, 2, srsEquivalent);

    assertFalse(validator.isValid(spatialReferenceSystems, context));
  }

  @Test
  void isValid_returnsFalse_forSrsWithIncorrectSrsEquivalentDimension() {
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    TestSpatialReferenceSystem srsEquivalent = createSrs(3, null);
    TestSpatialReferenceSystem srs = createSrs(2, 2);

    Map<Integer, SpatialReferenceSystem> spatialReferenceSystems = Map.of(1, srs, 2, srsEquivalent);

    assertFalse(validator.isValid(spatialReferenceSystems, context));
  }

  @Test
  void isValid_returnsFalse_forMissingSrsEquivalent() {
    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    TestSpatialReferenceSystem srs = createSrs(2, 2);

    Map<Integer, SpatialReferenceSystem> spatialReferenceSystems = Map.of(1, srs);

    assertFalse(validator.isValid(spatialReferenceSystems, context));
  }

  private TestSpatialReferenceSystem createSrs(Integer dimension, Integer equivalent) {
    TestSpatialReferenceSystem srs = new TestSpatialReferenceSystem();
    srs.setDimensions(dimension);
    srs.setEquivalent(equivalent);

    return srs;
  }
}
